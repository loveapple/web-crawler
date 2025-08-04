package com.happinesea.webcrawler;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import com.happinesea.webcrawler.entity.SiteCategory;
import com.happinesea.webcrawler.entity.SiteContents;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ContentsParser {
	public List<SiteContents> loadCategoryContentsList(SiteCategory category) {
		if (category == null) {
			return null;
		}
		try {
			Document doc = Jsoup.connect(category.getCategoryUrl()).get();

			// get contents list
			Elements elements = doc.select(category.getListRecordSelectId());
			if (elements == null || elements.isEmpty()) {
				return null;
			}

			List<SiteContents> result = new ArrayList<SiteContents>();
			for (Element element : elements) {
				SiteContents contents = new SiteContents();
				contents.setSiteCategory(category);
				contents.setTitle(element.select(category.getTitleRecordSelectId()).html());
				if (StringUtils.isBlank(contents.getTitle())) {
					continue;
				}
				contents.setUrl(element.select(category.getContentsUrlSelectId()).attr("href"));
				result.add(contents);
			}

			return result;

		} catch (Exception e) {
			log.warn(String.format("Invalid load category [%s], null).", category.getSiteCategoryId()), e);
			return null;
		}
	}

	public SiteContents loadContents(SiteContents contents) throws NotFoundContentsException {
		if (contents == null || StringUtils.isBlank(contents.getTitle()) || StringUtils.isBlank(contents.getUrl())) {
			throw new IllegalArgumentException(String.format("Invalid load contents is empty."));
		}
		if (contents.getSiteCategory() == null) {
			throw new IllegalArgumentException(
					String.format("Invalid category info for load contents url: %s", contents.getUrl()));
		}

		try {
			Elements elements = Jsoup.connect(contents.getUrl()).get()
					.select(contents.getSiteCategory().getBodySelectId());
			if (elements == null || elements.isEmpty()) {
				throw new NotFoundContentsException(String.format("site contents (%s) is empty.", contents.getUrl()));
			}

			StringBuilder sb = new StringBuilder(1024);
			for (Element element : elements) {
				// TODO 全文を読むリンクがある場合の取り込み
				// TODO 画像取り込み実装
				log.debug(element.html());
				sb.append(element.html());
			}
			contents.setContents(sb.toString());
			
			return contents;
		} catch (Exception e) {
			throw new NotFoundContentsException(String.format("Invalid load contents [%s], null).", contents.getUrl()),
					e);
		}
	}
}
