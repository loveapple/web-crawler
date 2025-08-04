package com.happinesea.webcrawler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.system.OutputCaptureExtension;

import com.happinesea.webcrawler.entity.SiteCategory;
import com.happinesea.webcrawler.entity.SiteContents;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

@ExtendWith(OutputCaptureExtension.class)
public class ContentsParserTest {

	private ContentsParser parser;

	private SiteCategory mockCategory;

	@BeforeEach
	void setUp() {
		parser = new ContentsParser();

		mockCategory = new SiteCategory();
		mockCategory.setCategoryUrl("http://test.com");
		mockCategory.setListRecordSelectId("#uamods-topics > ul > li");
		mockCategory.setTitleRecordSelectId(
				"li[data-ual-view-type=\"list\"] a div:nth-of-type(2) div:not(:has(*)):not([class^=\"yads\"])");
		mockCategory.setContentsUrlSelectId("li[data-ual-view-type=\"list\"] > a");
		mockCategory.setBodySelectId("#uamods > div.article_body");
	}

	@Test
	void testLoadCategoryContentsList_successful() throws Exception {
		// sample URL:https://news.yahoo.co.jp/topics/domestic
		String html = java.nio.file.Files.readString(
				new File("src/main/resources/test-contents/test-category-yahoo.html").toPath(), StandardCharsets.UTF_8);
		Document mockDoc = Jsoup.parse(html);

		try (MockedStatic<Jsoup> jsoupMock = mockStatic(Jsoup.class)) {
			Connection connectionMock = mock(Connection.class);
			jsoupMock.when(() -> Jsoup.connect("http://test.com")).thenReturn(connectionMock);
			when(connectionMock.get()).thenReturn(mockDoc);

			// Act
			List<SiteContents> result = parser.loadCategoryContentsList(mockCategory);

			// Assert
			assertThat(result).isNotEmpty();
			assertEquals(25, result.size());

			assertEquals("給食ない夏休み 子の食どう支える", result.get(0).getTitle());
			assertEquals("首相 8月末までに退陣表明の意向", result.get(1).getTitle());
			assertEquals("台風7号発生 24日から沖縄接近か", result.get(2).getTitle());
			assertEquals("24日帯広で40℃予想 歴史的高温か", result.get(23).getTitle());
			assertEquals("参院選 選挙妨害疑いなど6人逮捕", result.get(24).getTitle());

			assertEquals("https://news.yahoo.co.jp/pickup/6546562", result.get(0).getUrl());
			assertEquals("https://news.yahoo.co.jp/pickup/6546550", result.get(1).getUrl());
			assertEquals("https://news.yahoo.co.jp/pickup/6546543", result.get(2).getUrl());
			assertEquals("https://news.yahoo.co.jp/pickup/6546460", result.get(23).getUrl());
			assertEquals("https://news.yahoo.co.jp/pickup/6546453", result.get(24).getUrl());
		}
	}

	@Test
	void testLoadCategoryContentsList_whenJsoupThrows_logsWarning() throws Exception {
		// Arrange
		SiteCategory mockCategory = new SiteCategory();
		mockCategory.setCategoryUrl("http://fail.com");
		mockCategory.setListRecordSelectId(".item");
		mockCategory.setTitleRecordSelectId(".title");
		mockCategory.setContentsUrlSelectId(".link");
		mockCategory.setSiteCategoryId(123);

		// set up log capture
		Logger logger = (Logger) LoggerFactory.getLogger(ContentsParser.class);
		ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
		listAppender.start();
		logger.addAppender(listAppender);

		try (MockedStatic<Jsoup> jsoupMock = mockStatic(Jsoup.class)) {
			Connection mockConn = mock(Connection.class);
			jsoupMock.when(() -> Jsoup.connect("http://fail.com")).thenReturn(mockConn);
			when(mockConn.get()).thenThrow(new RuntimeException("connection failed"));

			// Act
			List<SiteContents> result = parser.loadCategoryContentsList(mockCategory);

			// Assert
			assertThat(result).isNull();

			// Log check
			List<ILoggingEvent> logs = listAppender.list;
			assertThat(logs.stream().anyMatch(e -> e.getFormattedMessage().contains("Invalid load category [123]")))
					.isTrue();
		}
	}

	@Test
	void testLoadCategoryContentsList_whenNullCategory_returnsNull() {
		// Act
		List<SiteContents> result = parser.loadCategoryContentsList(null);

		// Assert
		assertThat(result).isNull();
	}

	@Test
	void testLoadContents_successful() throws Exception {
		// Arrange
		String html = java.nio.file.Files.readString(
				new File("src/main/resources/test-contents/test-article-yahoo.html").toPath(), StandardCharsets.UTF_8);
		Document mockDoc = Jsoup.parse(html);

		SiteContents mockContents = new SiteContents();
		mockContents.setUrl("http://test.com/content");
		mockContents.setTitle("Dummy title");
		mockContents.setSiteCategory(mockCategory);

		try (MockedStatic<Jsoup> jsoupMock = mockStatic(Jsoup.class)) {
			Connection connectionMock = mock(Connection.class);
			jsoupMock.when(() -> Jsoup.connect("http://test.com/content")).thenReturn(connectionMock);
			when(connectionMock.get()).thenReturn(mockDoc);

			// Act
			SiteContents result = parser.loadContents(mockContents);

			// Assert
			assertThat(result).isNotNull();
			assertThat(result.getTitle()).isEqualTo("Dummy title"); // titleはそのまま保持
			assertThat(result.getContents()).contains("人事院＝東京・霞が関"); // test-article-yahoo.html に合わせて確認
		}
	}

	@Test
	void testLoadContents_whenJsoupFails_logsWarning() throws Exception {

		// Arrange
		String url = "http://example.com";
		SiteCategory category = new SiteCategory();
		category.setBodySelectId("div.content");

		SiteContents inputContents = new SiteContents();
		inputContents.setUrl(url);
		inputContents.setTitle("Test Title");
		inputContents.setSiteCategory(category);

		// JsoupがIOExceptionをスローするようモック
		Connection connectionMock = mock(Connection.class);
		when(connectionMock.get()).thenThrow(new IOException("Connection failed"));

		// モックの差し込み（静的メソッドをモックしている前提）
		try (MockedStatic<Jsoup> jsoupMock = mockStatic(Jsoup.class)) {
			jsoupMock.when(() -> Jsoup.connect(url)).thenReturn(connectionMock);

			// Act & Assert
			NotFoundContentsException ex = assertThrows(NotFoundContentsException.class, () -> {
				parser.loadContents(inputContents);
			});

			assertThat(ex.getMessage()).contains("Invalid load contents");
			assertThat(ex.getCause()).isInstanceOf(IOException.class);
		}
	}

	@Test
	void testLoadContents_whenNullInput_returnsNull() {
		assertThrows(IllegalArgumentException.class, () -> parser.loadContents(null));
	}

	/**
	 * @see https://coveralls.io/builds/74902235/source?filename=src%2Fmain%2Fjava%2Fcom%2Fhappinesea%2Fwebcrawler%2FContentsParser.java#L31
	 * @throws Exception
	 */
	@Test
	void testLoadCategoryContentsList_whenNoElements_returnsNull() throws Exception {
		Document mockDoc = Jsoup.parse("<html><body></body></html>");

		try (MockedStatic<Jsoup> jsoupMock = mockStatic(Jsoup.class)) {
			Connection connectionMock = mock(Connection.class);
			jsoupMock.when(() -> Jsoup.connect("http://test.com")).thenReturn(connectionMock);
			when(connectionMock.get()).thenReturn(mockDoc);

			List<SiteContents> result = parser.loadCategoryContentsList(mockCategory);
			assertThat(result).isNull();
		}
	}

	/**
	 * @see https://coveralls.io/builds/74902235/source?filename=src%2Fmain%2Fjava%2Fcom%2Fhappinesea%2Fwebcrawler%2FContentsParser.java#L62
	 */
	@Test
	void testLoadContents_whenSiteCategoryIsNull_throwsException() {
		SiteContents contents = new SiteContents();
		contents.setTitle("title");
		contents.setUrl("http://test.com");

		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
			parser.loadContents(contents);
		});

		assertThat(ex.getMessage()).contains("Invalid category info");
	}

	/**
	 * @see https://coveralls.io/builds/74902235/source?filename=src%2Fmain%2Fjava%2Fcom%2Fhappinesea%2Fwebcrawler%2FContentsParser.java#L70
	 * 
	 * @throws Exception
	 */
	@Test
	void testLoadContents_whenNoElementsFound_throwsNotFoundException() throws Exception {
	    // Arrange
	    SiteCategory category = new SiteCategory();
	    category.setBodySelectId("div.article"); // 適当なセレクタ

	    SiteContents contents = new SiteContents();
	    contents.setUrl("http://test.com/article");
	    contents.setTitle("Some title");
	    contents.setSiteCategory(category);

	    // 空の Elements を返すモック
	    Elements emptyElements = new Elements();

	    // Document モック
	    Document mockDoc = mock(Document.class);
	    when(mockDoc.select("div.article")).thenReturn(emptyElements);  // 必ず空を返すように

	    // Jsoup モック
	    try (MockedStatic<Jsoup> jsoupMock = mockStatic(Jsoup.class)) {
	        Connection connectionMock = mock(Connection.class);
	        jsoupMock.when(() -> Jsoup.connect("http://test.com/article")).thenReturn(connectionMock);
	        when(connectionMock.get()).thenReturn(mockDoc); // get() → mockDoc を返す

	        // Act & Assert
	        NotFoundContentsException ex = assertThrows(NotFoundContentsException.class, () -> {
	            parser.loadContents(contents);
	        });

	        // 期待される例外メッセージ（if の中からスローされたメッセージ）
	        assertThat(ex.getMessage()).contains("Invalid load contents [http://test.com/article], null");
	    }
	}


//	/**
//	 * @see https://coveralls.io/builds/74902235/source?filename=src%2Fmain%2Fjava%2Fcom%2Fhappinesea%2Fwebcrawler%2FContentsParser.java#L76
//	 * @throws Exception
//	 */
//	@Test
//	void testLoadCategoryContentsList_whenElementsContainNull_skipsNullElement() throws Exception {
//		// Arrange
//		mockCategory.setTitleRecordSelectId("div.title");
//		mockCategory.setContentsUrlSelectId("a.link");
//
//		Elements elements = new Elements();
//		elements.add(null); // null 要素
//		// 正常な要素
//		Element validElement = new Element("li");
//		validElement.appendChild(new Element("div").addClass("title").html("Valid Title"));
//		validElement.appendChild(new Element("a").addClass("link").attr("href", "https://example.com/valid"));
//		elements.add(validElement);
//
//		Document mockDoc = mock(Document.class);
//		when(mockDoc.select(anyString())).thenReturn(elements);
//
//		try (MockedStatic<Jsoup> jsoupMock = mockStatic(Jsoup.class)) {
//			Connection connectionMock = mock(Connection.class);
//			jsoupMock.when(() -> Jsoup.connect("http://test.com")).thenReturn(connectionMock);
//			when(connectionMock.get()).thenReturn(mockDoc);
//
//			// Act
//			List<SiteContents> result = parser.loadCategoryContentsList(mockCategory);
//
//			// Assert
//			assertThat(result).hasSize(1);
//			assertThat(result.get(0).getTitle()).isEqualTo("Valid Title");
//			assertThat(result.get(0).getUrl()).isEqualTo("https://example.com/valid");
//		}
//	}

}
