# Web Crawler Copilot 指示

## AIの振る舞い
- 常に日本語で返答する。
- AIの立場はベテランのJavaアーキテクトとSE、且つ、経験豊富なニュースサイトのプロダクトマネージャーである。

## アーキテクチャ概要
これはSpring Boot 3.5.3アプリケーションで、Spring Batchを使用してウェブクローリングを行います。Jsoupを使用してウェブサイトのコンテンツを解析し、MySQL（dev/prod）またはH2（test）にデータを保存します。主要なエンティティ：`SiteInfo`（サイト）-> `SiteCategory`（CSSセレクタ付きカテゴリ）-> `SiteContents`（解析された記事）。

データフロー：バッチジョブが`SiteInfoProcessPool`エントリを処理し、ステータスをPROCESSINGに更新、カテゴリURLを解析してコンテンツリストを抽出、`SiteContents`をNONE/SUCCESS/FAILステータスで保存。

## 主要コンポーネント
- **ContentsParser**: `SiteCategory`からのCSSセレクタ（例：`listRecordSelectId`、`titleRecordSelectId`）を使用してHTMLを解析。
- **BatchConfig**: `SiteInfoProcessPool`のリーダー/プロセッサ/ライターで`crawlJob`を定義。
- **SiteContentsService**: プロセスステータスの変更を管理し、`postContentsLimitCount`で投稿を制限。
- **Entities**: Lombok `@Data`を使用、カスタムenumコンバータで文字列ベースのDBストレージ。

## 開発ワークフロー
- **ビルド/テスト**: `./gradlew build` または `./gradlew test`。Jacocoでカバレッジ。
- **開発実行**: `SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun`（MySQL via env vars）。
- **本番実行**: `SPRING_PROFILES_ACTIVE=prod ./gradlew bootRun`（GitHub Secrets for DB/CMS）。
- **デバッグ**: `CrawlerComponents.siteInfoProcessor()`にブレークポイントを設定。
- **マイグレーション**: Flywayスクリプト in `src/main/resources/db/migration/`。テストでは無効。

## コードパターン
- **Enums**: `PersistableEnum`を実装し、文字列値（例：`ProcessStatus.NONE("1")`）。`@Convert(converter = ProcessStatusConverter.class)`を使用。
- **Converters**: JPAマッピングのために`EnumAttributeConverter<T>`を拡張。
- **Services**: ステータス更新に`@Transactional`。`@Autowired`でリポジトリを注入。
- **Parsing**: HTML抽出に`Jsoup.connect(url).get().select(selector)`。
- **Config**: `application.yml`の`web-crawler`下にカスタムプロパティ（例：`post-contents-limit-count`）。

## 規約
- エンティティフィールドはsnake_case DBカラム（例：`site_categoy_id` - タイポ注意）。
- プロファイル：`dev`（MySQL、Flyway有効）、`test`（H2、ddl-auto create）、`prod`（env vars）。
- ログ：Lombok `@Slf4j`、解析失敗で警告ログ。
- 依存関係：不明な機能のためのカスタム`rws-lib:1.0.0_preview`。

参照：解析例`ContentsParser.loadCategoryContentsList()`、ステータス処理`SiteContentsService.changSiteInfoProcess2Processing()`。