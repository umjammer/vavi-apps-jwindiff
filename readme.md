[![Release](https://jitpack.io/v/umjammer/vavi-apps-jwindiff.svg)](https://jitpack.io/#umjammer/vavi-apps-jwindiff)
[![Java CI](https://github.com/umjammer/vavi-apps-jwindiff/actions/workflows/maven.yml/badge.svg)](https://github.com/umjammer/vavi-apps-jwindiff/actions/workflows/maven.yml)
[![CodeQL](https://github.com/umjammer/vavi-apps-jwindiff/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/umjammer/vavi-apps-jwindiff/actions/workflows/codeql-analysis.yml)
![Java](https://img.shields.io/badge/Java-8-b07219)

# JWinDiff

JWinDiff のクラスを提供します．

## TODO

 * ~~JFileChooserTextField の TextField を CombpBox 化~~
 * ~~ヒストリ付 ComboBox~~
 * ~~ComboBox にポップアップメニュー~~
 * ヒストリのセーブ
 * ~~JFontChooser~~
 * ~~ディレクトリのソート~~
 * ~~ポップアップメニュー~~
 * JCheckBoxMenuItem と普通の JMeniItem のアラインメント → [EmptyIcon.java](C:\tmp\111\justsystem\ark11src\jp\co\justsystem\uiparts\EmptyIcon.java)
 * InputMap [see](http://www.hcn.zaq.ne.jp/no-ji/reseach/20000206.htm)
 * Moved line
 * Left ファイル Right ファイルのグラフィックを分ける
 * ~~正規表現マッチが微妙に違う~~
 * ダイアログのデフォルトフォーカス
 * デフォルトボタンも微妙に動作が違う
 * リストをテーブルに
 * ~~JFileChooserHistoryComboBox へのドラッグアンドドロップ [参考](http://www5.big.or.jp/~tera/Labo/Java2/j2dnd.html)~~
 * MVC 完全分離

## Tech Know

 * JPopupMenu がうまくいってなかったのはたぶん[こいつ](http://developer.java.sun.com/developer/bugParade/bugs/4632782.html)のせい
 * File#getCanonicalPath はめっちゃ重い
