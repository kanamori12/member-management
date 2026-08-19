# 会員管理システム

Spring Bootを使用して作成したWebベースの会員管理システムです。

会員情報の登録・一覧表示・検索・詳細表示・編集・削除に加え、タグ、ページング、並び替え、Spring Securityによるログイン認証、ADMIN / USERによる権限制御、ログインユーザー管理を実装しています。

テストでは、JUnit 5 / Mockitoによる単体テストに加えて、画面操作を中心としたブラックボックステストも実施しています。

---

## 1. 主な機能

### 会員管理

- 会員登録
- 会員一覧表示
- 会員詳細表示
- 会員情報編集
- 会員削除
- 会員検索
- タグ登録・編集・表示・検索
- ページング
- 並び替え

### 検索条件

以下の条件を組み合わせて検索できます。

- 名前
- 年齢
- 会員種別
- タグ

複数条件はAND条件として扱います。

会員種別：

```text
一般
ゴールド
```

### 並び替え

```text
ID昇順
ID降順
名前昇順
年齢昇順
```

---

## 2. ログイン機能

Spring Securityを使用したログイン認証を実装しています。

権限は以下の2種類です。

```text
ADMIN
USER
```

### ADMIN

ADMINはすべての会員データを操作できます。

- 全会員の一覧・検索・詳細表示
- 全会員の登録・編集・削除
- ログインユーザー管理
- ユーザー登録
- ユーザー編集
- ADMIN / USER権限変更
- パスワード変更
- ユーザー削除

### USER

USERは自分が所有している会員データのみ操作できます。

Service層でも所有者チェックを行い、URLを直接変更した場合でも他ユーザー所有の会員を取得・編集・削除できないようにしています。

```text
USER
  ↓
会員ID + 所有者ユーザー名
  ↓
対象会員取得
```

---

## 3. ログインユーザー管理

ADMINはログインユーザー管理画面からユーザーを管理できます。

- ユーザー一覧
- ユーザー登録
- ユーザー編集
- ADMIN / USER権限変更
- パスワード変更
- ユーザー削除

---

## 4. ADMIN保護

管理者が存在しなくなることを防ぐため、以下の制御を実装しています。

- 自分自身の削除禁止
- 自分自身のADMIN権限解除禁止
- 最後のADMIN削除禁止
- 最後のADMIN権限変更禁止

これにより、管理操作によってシステム上のADMINが0人になることを防止しています。

---

## 5. パスワード管理

パスワードは平文では保存せず、Spring Securityの`PasswordEncoder`を使用してハッシュ化して保存します。

```text
入力パスワード
     ↓
PasswordEncoder
     ↓
ハッシュ化
     ↓
データベース保存
```

パスワード変更時も新しいパスワードをハッシュ化して保存します。

---

## 6. タグ機能

会員に複数のタグを設定できます。

主な仕様：

- 複数タグ登録
- 前後空白の除去
- 空タグの除外
- 同一タグの重複防止
- タグ追加・解除
- 一覧画面でのタグ表示
- 詳細画面でのタグ表示
- タグによる会員検索

タグ検索は名前・年齢・会員種別などの条件と組み合わせてAND検索できます。

---

## 7. 例外処理

独自例外を使用して、存在しないデータや許可されていない操作を処理しています。

```text
MemberNotFoundException
AppUserNotFoundException
UserOperationException
```

`GlobalExceptionHandler`で例外を捕捉し、共通エラー画面へエラーメッセージを渡します。

また、USERがADMIN専用画面へアクセスした場合はアクセス拒否画面を表示します。

---

## 8. UI

画面には以下を使用しています。

```text
Thymeleaf
Bootstrap 5
CSS
```

共通部分はThymeleaf Fragmentとして分離しています。

```text
fragments/
├── head.html
└── header.html
```

---

## 9. アプリケーション構成

```text
Browser
   ↓
Controller
   ↓
Service
   ↓
Repository
   ↓
Database
```

### Controller

HTTPリクエストを受け取り、Serviceを呼び出して画面へデータを渡します。

### Service

- 会員管理
- タグ処理
- 所有者チェック
- ADMIN / USER制御
- ユーザー管理
- ADMIN保護
- パスワードハッシュ化

### Repository

Spring Data JPAを使用してデータベースアクセスを行います。

---

## 10. 会員検索

Spring Data JPAの`Specification`を使用して検索条件を組み立てています。

```text
名前
年齢
会員種別
タグ
```

USERの場合は所有者条件も追加します。

```text
検索条件
   +
owner.username = ログインユーザー
```

これにより、USERの検索結果には自分が所有する会員のみ表示されます。

---

## 11. ページング

Spring Dataの`Page` / `Pageable`を使用し、1ページ5件でページングしています。

検索時も、

```text
検索条件
+
タグ条件
+
ページ番号
+
ページサイズ
+
並び順
```

を組み合わせ、ページ移動後も検索条件・タグ条件・並び順を保持します。

---

## 12. N+1問題への対応

会員一覧でタグを表示する際、会員ごとにタグ取得SQLが発行されるN+1問題を確認しました。

一覧取得では会員とタグをまとめて取得するようにし、ページング時にも不要なSQLが繰り返し発行されないよう改善しています。

この過程でHibernate SQLログを確認し、遅延ロードとN+1問題の挙動を確認しました。

---

## 13. 使用技術

| 分類 | 技術 |
|---|---|
| 言語 | Java 25 |
| Framework | Spring Boot |
| MVC | Spring MVC |
| Template Engine | Thymeleaf |
| Security | Spring Security |
| ORM | Spring Data JPA / Hibernate |
| Database | PostgreSQL |
| Build Tool | Maven |
| UI | Bootstrap 5 / CSS |
| Unit Test | JUnit 5 |
| Mock | Mockito |
| Coverage | JaCoCo |
| Version Control | Git |
| Repository | GitHub |

---

## 14. テスト

### 単体テスト

JUnit 5 / Mockitoを使用して単体テストを実装しています。

Service層だけでなく、Controller、Config、Exception、Modelもテスト対象としています。

主な観点：

- 正常系・異常系
- 会員CRUD
- 検索・ページング・ソート
- ADMIN / USER分岐
- USER所有者制御
- ログインユーザー管理
- パスワードハッシュ化
- ユーザー名重複
- 自分自身の操作制限
- 最後のADMIN保護
- 例外処理
- Security設定
- 初期ADMIN作成

詳細：

```text
TESTING.md
```

### ブラックボックステスト

実際の画面操作を中心に、内部実装を意識せず外部仕様からテストを実施しました。

主な観点：

- ログイン / ログアウト
- 会員CRUD
- Validation
- 年齢の境界値
- 検索・複数条件AND検索
- タグ登録・編集・検索
- 並び替え
- ページング
- ADMIN / USER権限制御
- USER所有者制御
- ログインユーザー管理
- パスワード変更
- 403 / 共通エラー画面
- URL直接アクセスによる不正操作防止

実施可能なブラックボックステストはすべてOK、NGは0件です。

最後のADMINに関する2ケースは、画面上では自己操作禁止の条件と重複するため保留とし、Service層のJUnitテストで補完しています。

ブラックボックステスト中には、会員詳細画面にタグ表示がないことを発見し、実装修正後にテストケースを追加して再テストしました。

```text
docs/test/
├── test-matrix.md
└── blackbox-test.md
```

---

## 15. JaCoCoカバレッジ

JaCoCoによるカバレッジ計測を実施しています。

| 対象 | Instruction Coverage | Branch Coverage |
|---|---:|---:|
| Service | 100% | 100% |
| Controller | 100% | 100% |
| Model | 100% | n/a |
| Config | 100% | 100% |
| Exception | 100% | n/a |
| 全体 | 99% | 100% |

全体のInstruction Coverageが99%なのは、Spring Bootのエントリポイントである`DemoApplication.main()`を単体テスト対象外としているためです。

カバレッジ数値だけを100%にすることを目的としたテストは追加していません。

---

## 16. テスト実行

```powershell
mvn test
```

クリーン後：

```powershell
mvn clean test
```

成功すると、

```text
BUILD SUCCESS
```

と表示されます。

---

## 17. ビルド

```powershell
mvn clean package
```

---

## 18. アプリケーション起動

```powershell
mvn spring-boot:run
```

起動後：

```text
http://localhost:8080
```

未ログインの場合はログイン画面へ遷移します。

---

## 19. プロジェクト構成

```text
demo/
├── docs/
│   └── test/
│       ├── test-matrix.md
│       └── blackbox-test.md
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/demo/
│   │   │       ├── config/
│   │   │       ├── controller/
│   │   │       ├── exception/
│   │   │       ├── model/
│   │   │       ├── repository/
│   │   │       └── service/
│   │   └── resources/
│   │       ├── static/
│   │       ├── templates/
│   │       └── application.properties
│   └── test/
│       └── java/
│           └── com/example/demo/
├── TESTING.md
├── README.md
└── pom.xml
```

---

## 20. テストドキュメント

| ファイル | 内容 |
|---|---|
| `TESTING.md` | JUnit / Mockitoによる単体テスト仕様・結果 |
| `docs/test/test-matrix.md` | ブラックボックステストの観点・網羅性 |
| `docs/test/blackbox-test.md` | ブラックボックステストケース・実施結果 |

単体テストとブラックボックステストを分けて記録することで、内部ロジックと利用者視点の両方から動作を確認しています。
