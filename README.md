[![CircleCI](https://dl.circleci.com/status-badge/img/circleci/YCfPJGARVj7bQmffvDjAjK/SrPbgKgrfe5d1EkJcJU1L2/tree/main.svg?style=svg)](https://dl.circleci.com/status-badge/redirect/circleci/YCfPJGARVj7bQmffvDjAjK/SrPbgKgrfe5d1EkJcJU1L2/tree/main)
[![Coverage Status](https://coveralls.io/repos/github/loveapple/web-crawler/badge.svg?branch=main)](https://coveralls.io/github/loveapple/web-crawler?branch=main)

[![X (formerly Twitter) Follow](https://img.shields.io/twitter/follow/ThumbJava)](https://x.com/ThumbJava)

## 環境設定

このプロジェクトは、データベースやCMS接続などの機密設定に環境変数を使用します。これらの値をGitにコミットしないでください。

### 開発環境 (Codespaces)
環境変数は `.devcontainer/devcontainer.json` で設定されます。MySQLが自動的にセットアップされます。

### 本番環境 (GitHub Actions)
GitHubリポジトリの設定で以下のSecretsを設定してください：
- `DB_URL`: データベース接続URL
- `DB_USERNAME`: データベースユーザー名
- `DB_PASSWORD`: データベースパスワード
- `CMS_HOST`: CMSホストURL
- `POST_LIMIT`: コンテンツ投稿制限数 (オプション、デフォルト: 10)

### ローカル開発
プロジェクトルートに `.env` ファイルを作成してください (Gitで無視されます)：
```
DB_URL=jdbc:mysql://localhost:3306/webcrawler
DB_USERNAME=root
DB_PASSWORD=password
CMS_HOST=your-cms-host
POST_LIMIT=10
```

実行: `SPRING_PROFILES_ACTIVE=prod ./gradlew bootRun`

## ライセンス

番创知库(loveapple.cn)


