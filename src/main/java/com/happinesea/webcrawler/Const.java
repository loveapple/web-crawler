package com.happinesea.webcrawler;

import lombok.Getter;

public class Const {
	@Getter
	public static enum ProcessStatus{
		NONE("1"),
		SUCCESS("0"),
		FAIL("9"),
		PROCESSING("2");

		private final String value;
		private ProcessStatus(String value) {
			this.value = value;
		}
	}
	@Getter
	public static enum DeleteFlg{
		ON("1"),
		OFF("0");

		private final String value;
		private DeleteFlg(String value) {
			this.value = value;
		}
	}
	@Getter
	public static enum ContentsType{
		HTML("1"),
		Wordpress("2");

		private final String value;
		private ContentsType(String value) {
			this.value = value;
		}
	}
}
