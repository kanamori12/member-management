# テスト仕様書

## 1. 概要

本ドキュメントでは、会員管理システムで実施しているJUnitによる単体テストについて記載します。

Service層を中心に、Controller、Config、Exception、Modelも含めて以下の観点をテストしています。

-   正常系
-   異常系
-   ADMIN / USER の権限制御
-   会員データの所有者制御
-   ユーザー登録
-   ユーザー情報更新
-   ユーザー削除
-   パスワード変更
-   最後のADMIN保護
-   ページング
-   ソート
-   存在しないデータへのアクセス

RepositoryはMockitoでモック化し、Service層のビジネスロジックをDBから切り離して検証しています。

------------------------------------------------------------------------

# 2. 使用技術

テストでは主に以下を使用しています。

  技術                 用途
  -------------------- ------------------------------
  JUnit 5              テストの実行・アサーション
  Mockito              Repositoryなどのモック化
  MockitoExtension     JUnit 5とMockitoの連携
  ArgumentCaptor       Repositoryへ渡された値の確認
  Spring Data `Page`   ページング処理の確認
  Spring Data `Sort`   並び順の確認

------------------------------------------------------------------------

# 3. テスト対象

Service層では主に以下のクラスをテストしています。

``` text
MemberServiceImpl
AppUserService
```

さらに、Controller、Config、Exception、Modelについてもテストを実施しています。

主なテスト対象パッケージ：

``` text
src/test/java/com/example/demo/
├── config/
├── controller/
├── exception/
├── model/
└── service/
```

Controllerでは`MemberController`、`AppUserController`、`AuthController`を対象とし、
Configでは`SecurityConfig`、`DataInitializer`を対象としています。

------------------------------------------------------------------------

# 4. MemberServiceImplTest

## 4.1 テスト対象

``` text
MemberServiceImpl
```

会員情報に関する以下の処理をテストします。

-   登録
-   一覧取得
-   検索
-   詳細取得
-   更新
-   削除
-   ADMIN / USERによるアクセス範囲
-   所有者チェック
-   ページング
-   ソート

------------------------------------------------------------------------

# 5. 会員登録テスト

## 5.1 `register_会員を登録できる`

### テスト目的

会員登録処理を実行した場合に、Repositoryの`save()`が正しく呼び出されることを確認します。

### 事前条件

以下の会員を作成します。

  項目       値
  ---------- ------
  ID         1
  名前       田中
  年齢       30
  会員種別   一般

### 実行

``` java
memberService.register(member);
```

### 期待結果

``` java
memberRepository.save(member);
```

が1回呼び出されること。

### 確認内容

``` java
verify(memberRepository)
        .save(member);
```

------------------------------------------------------------------------

# 6. 会員一覧取得テスト

## 6.1 `findAll_ADMINなら全会員を取得できる`

### テスト目的

ADMINの場合、所有者に関係なく全会員を取得できることを確認します。

### 事前条件

以下の2件の会員が存在すると仮定します。

    ID 名前     年齢 会員種別
  ---- ------ ------ ----------
     1 田中       30 一般
     2 佐藤       40 ゴールド

ページ条件：

``` text
ページ番号：0
1ページ：5件
```

### 実行

``` java
memberService.findAll(
        "admin",
        true,
        pageable);
```

`true`はADMINであることを表します。

### 期待結果

ADMINなので、

``` java
memberRepository.findAll(pageable);
```

が使用されること。

取得件数：

``` text
2件
```

### 追加確認

USER用の、

``` java
findByOwnerUsername()
```

が呼び出されないことも確認します。

------------------------------------------------------------------------

## 6.2 `findAll_USERなら自分の会員だけ取得する`

### テスト目的

USERの場合、自分が所有している会員だけを取得することを確認します。

### ログインユーザー

``` text
user
```

### 実行

``` java
memberService.findAll(
        "user",
        false,
        pageable);
```

### 期待結果

以下が使用されます。

``` java
memberRepository.findByOwnerUsername(
        "user",
        pageable);
```

ADMIN用の、

``` java
memberRepository.findAll(pageable);
```

は呼び出されません。

### このテストで確認できること

USERが一覧画面から他のユーザーが所有している会員を取得できない設計になっていることを確認します。

------------------------------------------------------------------------

# 7. 会員検索・ソートテスト

`MemberServiceImpl.search()`では検索条件とともに並び順を指定できます。

現在テストしている並び順は以下です。

  sort値      項目   方向
  ----------- ------ ------
  `idAsc`     ID     昇順
  `idDesc`    ID     降順
  `nameAsc`   名前   昇順
  `ageAsc`    年齢   昇順
  不明な値    ID     昇順

------------------------------------------------------------------------

## 7.1 `search_ID昇順で検索できる`

### 条件

``` text
sort = idAsc
```

### 実行

``` java
memberService.search(
        condition,
        "admin",
        true,
        pageable);
```

### 期待結果

Repositoryへ渡されるPageableのSortが、

``` text
id ASC
```

となること。

### ページング確認

元のPageableの、

``` text
pageNumber = 0
pageSize   = 5
```

が維持されることも確認します。

------------------------------------------------------------------------

## 7.2 `search_ID降順を指定できる`

### 条件

``` text
sort = idDesc
```

### 期待結果

``` text
id DESC
```

になること。

確認対象：

``` java
Sort.Direction.DESC
```

------------------------------------------------------------------------

## 7.3 `search_名前昇順を指定できる`

### 条件

``` text
sort = nameAsc
```

### 期待結果

``` text
name ASC
```

になること。

------------------------------------------------------------------------

## 7.4 `search_年齢昇順を指定できる`

### 条件

``` text
sort = ageAsc
```

### 期待結果

``` text
age ASC
```

になること。

------------------------------------------------------------------------

## 7.5 `search_不明な並び順ならID昇順になる`

### テスト目的

想定していないsort値が指定された場合でも、安全なデフォルト値が使用されることを確認します。

### 条件

``` text
sort = unknown
```

### 期待結果

デフォルトとして、

``` text
id ASC
```

が使用されます。

------------------------------------------------------------------------

# 8. 会員詳細取得テスト

## 8.1 `findById_ADMINなら会員を取得できる`

### テスト目的

ADMINがIDを指定して会員を取得できることを確認します。

### 対象会員

  項目       値
  ---------- ------
  ID         1
  名前       田中
  年齢       30
  会員種別   一般

### 実行

``` java
memberService.findById(
        1L,
        "admin",
        true);
```

### 期待結果

ADMINなので、

``` java
memberRepository.findById(1L);
```

が使用されます。

取得した会員について、

``` text
ID       = 1
名前     = 田中
年齢     = 30
会員種別 = 一般
```

であることを確認します。

------------------------------------------------------------------------

## 8.2 `findById_ADMINで存在しない会員なら例外になる`

### 条件

``` text
ID = 999
```

が存在しないとします。

### 実行

``` java
memberService.findById(
        999L,
        "admin",
        true);
```

### 期待結果

``` text
MemberNotFoundException
```

が発生します。

------------------------------------------------------------------------

## 8.3 `findById_USERなら自分の会員を取得できる`

### テスト目的

USERが自分の所有する会員を取得できることを確認します。

### ログインユーザー

``` text
user
```

### 実行

``` java
memberService.findById(
        1L,
        "user",
        false);
```

### 期待結果

USERの場合は単純なID検索ではなく、

``` java
memberRepository.findByIdAndOwnerUsername(
        1L,
        "user");
```

が使用されます。

また、

``` java
memberRepository.findById(1L);
```

は使用されません。

------------------------------------------------------------------------

## 8.4 `findById_USERが他人の会員を取得しようとすると例外になる`

### テスト目的

USERが自分以外のユーザーが所有する会員を取得できないことを確認します。

### 条件

``` text
ログインユーザー = user
会員ID           = 2
```

`user`がID=2の所有者ではないと仮定します。

### 期待結果

``` text
MemberNotFoundException
```

が発生します。

### セキュリティ上の意味

例えばURLを直接、

``` text
/detail?id=2
```

のように変更した場合でも、Service層で所有者チェックが行われる設計をテストしています。

------------------------------------------------------------------------

# 9. 会員更新テスト

## 9.1 `update_ADMINなら会員を更新できる`

### 更新前

``` text
名前     = 田中
年齢     = 30
会員種別 = 一般
```

### 更新後

``` text
名前     = 田中太郎
年齢     = 31
会員種別 = ゴールド
```

### 期待結果

既存Entityの、

``` java
name
age
memberType
```

がすべて変更されること。

------------------------------------------------------------------------

## 9.2 `update_USERなら自分の会員を更新できる`

### テスト目的

USERが自分の所有する会員であれば更新できることを確認します。

### Repository検索

``` java
findByIdAndOwnerUsername(
        1L,
        "user");
```

によって対象会員を取得します。

### 期待結果

``` text
田中
30
一般
```

から、

``` text
田中太郎
31
ゴールド
```

へ変更されること。

------------------------------------------------------------------------

## 9.3 `update_USERが他人の会員を更新しようとすると例外になる`

### テスト目的

USERが他人の会員を書き換えられないことを確認します。

### 条件

``` text
ログインユーザー = user
対象ID           = 2
```

所有者検索結果：

``` java
Optional.empty()
```

### 期待結果

``` text
MemberNotFoundException
```

が発生します。

------------------------------------------------------------------------

# 10. 会員削除テスト

## 10.1 `delete_ADMINなら会員を削除できる`

### 実行

``` java
memberService.delete(
        1L,
        "admin",
        true);
```

### 期待結果

ADMINはIDで対象会員を取得し、

``` java
memberRepository.delete(member);
```

が実行されます。

------------------------------------------------------------------------

## 10.2 `delete_ADMINで存在しない会員なら例外になる`

### 条件

``` text
ID = 999
```

### 期待結果

``` text
MemberNotFoundException
```

が発生します。

さらに、

``` java
memberRepository.delete()
```

が実行されないことを確認します。

------------------------------------------------------------------------

## 10.3 `delete_USERなら自分の会員を削除できる`

### 条件

``` text
ログインユーザー = user
会員ID           = 1
```

### 期待結果

``` java
findByIdAndOwnerUsername(
        1L,
        "user");
```

によって対象会員が取得され、

``` java
memberRepository.delete(member);
```

が実行されます。

------------------------------------------------------------------------

## 10.4 `delete_USERが他人の会員を削除しようとすると例外になる`

### 条件

``` text
ログインユーザー = user
会員ID           = 2
```

対象会員がuserの所有物ではない場合、

``` text
MemberNotFoundException
```

が発生します。

さらに、

``` java
memberRepository.delete()
```

が呼び出されないことを確認します。

------------------------------------------------------------------------

# 11. AppUserServiceTest

## 11.1 テスト対象

``` text
AppUserService
```

ログインユーザー管理に関する以下の機能をテストします。

-   ユーザー登録
-   ユーザー名重複チェック
-   ユーザー情報更新
-   権限変更
-   自分自身に対する操作制限
-   最後のADMIN保護
-   ユーザー削除
-   パスワード変更
-   ID検索
-   ユーザー名検索

------------------------------------------------------------------------

# 12. ログインユーザー登録テスト

## 12.1 パスワードをハッシュ化して登録できる

### 入力

``` text
username = testuser
password = password123
role     = USER
```

### PasswordEncoder

Mockitoによって、

``` java
passwordEncoder.encode("password123")
```

の結果を、

``` text
hashedPassword
```

とします。

### 実行

``` java
appUserService.register(form);
```

### 期待結果

Repositoryへ保存されるAppUserが、

``` text
username = testuser
password = hashedPassword
role     = USER
```

となること。

### 重要な確認

平文の、

``` text
password123
```

ではなく、ハッシュ化後の、

``` text
hashedPassword
```

がEntityに設定されることを確認します。

------------------------------------------------------------------------

# 13. ユーザー名重複テスト

## 13.1 同じユーザー名が存在する場合

### 条件

すでに、

``` text
testuser
```

というユーザーが存在しているとします。

### 実行

同じユーザー名で登録を実行します。

### 期待結果

``` text
UserOperationException
```

が発生します。

さらに、

``` java
appUserRepository.save()
```

が呼び出されないことを確認します。

------------------------------------------------------------------------

# 14. ユーザー情報更新テスト

## 14.1 他ユーザーの権限を変更できる

### 更新前

``` text
username = user
role     = USER
```

### 更新後

``` text
username = user
role     = ADMIN
```

### 操作者

``` text
admin
```

### 期待結果

対象ユーザーの権限が、

``` text
USER → ADMIN
```

へ変更され、

``` java
appUserRepository.save(appUser);
```

が実行されます。

------------------------------------------------------------------------

# 15. 自分自身のADMIN権限変更禁止

## 15.1 自分をADMINからUSERへ変更しようとした場合

### ログインユーザー

``` text
admin
```

### 対象ユーザー

``` text
username = admin
role     = ADMIN
```

### 変更内容

``` text
ADMIN → USER
```

### 期待結果

``` text
UserOperationException
```

が発生します。

さらに、

``` java
appUserRepository.save()
```

は実行されません。

### 目的

ログイン中のADMINが誤って自分自身から管理権限を外してしまうことを防止します。

------------------------------------------------------------------------

# 16. 最後のADMIN保護

AppUserServiceでは、システム上からADMINが0人になることを防止しています。

## 16.1 最後のADMINをUSERへ変更

### 条件

``` text
ADMIN数 = 1
```

### 操作

``` text
ADMIN → USER
```

### 期待結果

``` text
UserOperationException
```

が発生します。

------------------------------------------------------------------------

## 16.2 ADMINが複数存在する場合

### 条件

``` text
ADMIN数 >= 2
```

の場合、他のADMINをUSERへ変更しても最低1人のADMINが残ります。

そのため、条件を満たしていれば変更できます。

------------------------------------------------------------------------

# 17. ユーザー削除テスト

## 17.1 自分自身を削除しようとした場合

### ログインユーザー

``` text
admin
```

### 削除対象

``` text
admin
```

### 期待結果

``` text
UserOperationException
```

が発生します。

さらに、

``` java
appUserRepository.delete()
```

は実行されません。

------------------------------------------------------------------------

## 17.2 最後のADMINを削除しようとした場合

### 条件

``` text
ADMIN数 = 1
```

### 期待結果

最後のADMINは削除できず、

``` text
UserOperationException
```

が発生します。

### 目的

システム上にADMINが存在しない状態になることを防止します。

------------------------------------------------------------------------

## 17.3 ADMINが複数存在する場合

ADMINが2人以上存在する場合は、他のADMINを削除しても最低1人のADMINが残ります。

条件を満たしていれば削除可能です。

------------------------------------------------------------------------

# 18. パスワード変更テスト

## 18.1 パスワードをハッシュ化して更新できる

### 更新前

``` text
password = oldPassword
```

### 入力

``` text
password        = newPassword
confirmPassword = newPassword
```

### PasswordEncoder

``` java
passwordEncoder.encode("newPassword")
```

の結果：

``` text
hashedNewPassword
```

### 実行

``` java
appUserService.updatePassword(form);
```

### 期待結果

Entityに保存されるパスワード：

``` text
hashedNewPassword
```

### Repository

``` java
appUserRepository.save(appUser);
```

が実行されることを確認します。

------------------------------------------------------------------------

# 19. 存在しないログインユーザーの取得

## 19.1 `findById_存在しないユーザーIDなら例外になる`

### 条件

``` text
ID = 999
```

Repository：

``` java
Optional.empty()
```

### 実行

``` java
appUserService.findById(999L);
```

### 期待結果

``` text
AppUserNotFoundException
```

が発生します。

------------------------------------------------------------------------

# 20. 権限制御のテストまとめ

本システムではADMINとUSERで会員データへのアクセス範囲が異なります。

  操作              ADMIN   USER
  ---------------- ------- ------
  全会員一覧          ○      ×
  自分の会員一覧      ○      ○
  任意会員の詳細      ○      ×
  自分の会員詳細      ○      ○
  任意会員の更新      ○      ×
  自分の会員更新      ○      ○
  任意会員の削除      ○      ×
  自分の会員削除      ○      ○

USERの場合、

``` java
findByIdAndOwnerUsername(
        id,
        username);
```

を使用することで、IDだけでは会員を取得できない設計としています。

------------------------------------------------------------------------

# 21. ADMIN管理のテストまとめ

  操作                               期待結果
  ---------------------------------- ----------------
  USERをADMINへ変更                  許可
  他のADMINをUSERへ変更              条件付きで許可
  自分自身をADMIN→USER               禁止
  最後のADMINをUSERへ変更            禁止
  自分自身を削除                     禁止
  最後のADMINを削除                  禁止
  ADMINが複数いる状態で他ADMIN削除   許可

これにより、管理操作によってADMINが意図せず0人になることを防止します。

------------------------------------------------------------------------

# 22. 例外テストまとめ

  対象           条件                  期待される例外
  -------------- --------------------- ----------------------------
  会員取得       存在しないID          `MemberNotFoundException`
  USER会員取得   他人の会員            `MemberNotFoundException`
  USER会員更新   他人の会員            `MemberNotFoundException`
  USER会員削除   他人の会員            `MemberNotFoundException`
  ユーザー取得   存在しないID          `AppUserNotFoundException`
  ユーザー登録   ユーザー名重複        `UserOperationException`
  権限変更       自分自身のADMIN解除   `UserOperationException`
  権限変更       最後のADMIN           `UserOperationException`
  ユーザー削除   自分自身              `UserOperationException`
  ユーザー削除   最後のADMIN           `UserOperationException`

------------------------------------------------------------------------

# 23. Mockitoで確認している内容

本テストでは、単純に戻り値だけを見るのではなく、Repositoryが正しく使用されているかも確認しています。

## verify

例えば、

``` java
verify(memberRepository)
        .delete(member);
```

によって、削除処理が実際にRepositoryへ依頼されたことを確認します。

------------------------------------------------------------------------

## never

例えば、

``` java
verify(memberRepository, never())
        .delete(any(Member.class));
```

によって、異常系で削除処理が実行されていないことを確認します。

これは、

``` text
例外が発生した
```

だけではなく、

``` text
DB更新処理まで到達していない
```

ことを確認するために使用しています。

------------------------------------------------------------------------

## ArgumentCaptor

検索処理では、

``` java
ArgumentCaptor<Pageable>
```

を使用しています。

これにより、ServiceからRepositoryへ実際に渡されたPageableを取得して、

``` text
ページ番号
ページサイズ
ソート項目
昇順 / 降順
```

を確認しています。

------------------------------------------------------------------------

# 24. テスト実行方法

プロジェクトルートで以下を実行します。

Windows PowerShell：

``` powershell
mvn test
```

クリーン後に実行する場合：

``` powershell
mvn clean test
```

------------------------------------------------------------------------

# 25. テスト成功時

すべてのテストが成功すると、Mavenの最後に、

``` text
BUILD SUCCESS
```

と表示されます。

現在、作成済みのService層単体テストについて`BUILD SUCCESS`になることを確認しています。

------------------------------------------------------------------------

# 26. 現在のテスト範囲

現在はService層だけでなく、Controller、Config、Exception、Modelまでテスト対象を拡張しています。

``` text
Controller   ← 単体テスト実施
    ↓
Service      ← 単体テスト実施
    ↓
Repository   ← Mockitoでモック化
    ↓
Database     ← Service単体テストでは使用しない

Config       ← Security設定・初期ADMIN作成をテスト
Exception    ← 例外ハンドリングをテスト
Model        ← Entity / Form等の基本動作をテスト
```

Service層ではRepositoryをMockitoでモック化することで、
DBから切り離してビジネスロジックを高速に検証しています。

------------------------------------------------------------------------

## Controllerテスト

Controllerでは、正常な画面遷移、Modelへの値設定、Validationエラー、
登録・更新・削除後のリダイレクト、ログインユーザー名の取得、 ADMIN /
USER判定、Serviceの呼び出しなどを確認しています。

対象Controller：

``` text
MemberController
AppUserController
AuthController
```

ControllerパッケージのInstruction Coverage / Branch
Coverageともに100%です。

------------------------------------------------------------------------

## Configテスト

`SecurityConfig`と`DataInitializer`をテストしています。

`DataInitializer`では、初期ADMINが存在しない場合に登録する分岐と、
すでに存在する場合に再登録しない分岐の両方を確認しています。

ConfigパッケージのInstruction Coverage / Branch Coverageともに100%です。

------------------------------------------------------------------------

## Exceptionテスト

`GlobalExceptionHandler`について、`MemberNotFoundException`、
`IllegalArgumentException`、`AppUserNotFoundException`、
`UserOperationException`を処理した場合に`error`画面へ遷移し、
エラーメッセージがModelへ設定されることを確認しています。

ExceptionパッケージのInstruction Coverageは100%です。

------------------------------------------------------------------------

## Modelテスト

Entity、Form、検索条件などのModelクラスについて、 getter /
setter等を含む基本動作を確認しています。

ModelパッケージのInstruction Coverageは100%です。

------------------------------------------------------------------------

# 27. JaCoCoカバレッジ

JaCoCoを使用してテストカバレッジを確認しています。

  対象           Instruction Coverage   Branch Coverage
  ------------ ---------------------- -----------------
  Service                        100%              100%
  Controller                     100%              100%
  Model                          100%               n/a
  Config                         100%              100%
  Exception                      100%               n/a
  全体                            99%              100%

全体のInstruction Coverageが99%なのは、Spring
Bootのエントリポイントである
`DemoApplication.main()`を単体テスト対象外としているためです。

`DemoApplication.main()`はアプリケーション起動用コードであり、
カバレッジ数値のみを100%にすることを目的としたテストは追加していません。

------------------------------------------------------------------------

# 28. テスト方針まとめ

本システムでは、正常に処理できることだけではなく、

``` text
「実行してはいけない操作が、本当に実行されないか」
```

も重要なテスト対象としています。

特に、USERによる他人の会員操作防止、自分自身の削除防止、
自分自身のADMIN権限解除防止、最後のADMIN削除・権限解除防止、
存在しないデータへのアクセス、ユーザー名重複登録防止などの
異常系・権限制御をテストしています。

さらにController、Config、Exception、Modelまでテスト範囲を広げ、
主要パッケージのInstruction Coverage 100%、
条件分岐を持つ主要パッケージのBranch Coverage 100%を確認しています。
