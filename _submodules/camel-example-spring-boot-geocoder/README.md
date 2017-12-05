# camel-example-spring-boot-geocoder
Apache CamelのSpring Bootインテグレーションサンプル（GeoCode）

これをベースにApache Camelを勉強してみる。

# 困ったこと

## GeoCoderのProxy越え

社内でやっていると、どうしてもProxyの中にいるので、Proxyを越えるのは必須。
当然APIとして考慮されていて、以下の方法で対応できる。

```java
.get().description("Geocoder address lookup")
	// パラメータに、'proxyHost'、'proxyPort'を指定する
	.toD("geocoder:address:${header.address}?proxyHost=<host.name>&proxyPort=8080");
```
で、GeoCodeに限らずなんだけど、いちいちパラメータに書くのは嫌だなぁと。
基本的には、環境変数等の「http.proxyHost」、「http.proxyPort」を使ってくれれば良いだけなんだけど。

## HttpComponentのProxy越え

で、同じことはRESTするときとかも言える。普通に考えると、環境変数、およびそれを元にした、システムプロパティの
- http.proxyHost
- http.proxyPort
- http.nonProxyHosts
- https....（省略）

を使って頂ければと。ちょっとGoogleさんに聞いてみると、[このあたり](https://qiita.com/laqiiz/items/7b392e6c603a9b805635)に、システムプロパティはダメで、CamelContextのプロパティに設定しないとダメだそうな。
世の中そういうもんなんだ。。。

## でも、nonProxyHostsは何処へ

上記の方法で、CamelContextにProxyの設定を行えば、デフォルトでProxyが使われることになって、宜しいんですけど、「いやいや、nonProxyHostsの設定無視かよっ」ということになっちゃうようで。やりたいことを整理すると、
1. 単にProxy設定を一元化したい。
2. 通信先に応じて、nonProxyHostsを考慮したProxy設定をしたい。

ってことなので、ちょっと確認＆イジってみた。

## nonProxyHostsを考慮した動的なProxy設定やってみる

HTTP(S)は、Apache HttpClientを利用しているようで、CamelはこれをHttpComponentで生成するHttpEndPoint#createHttpClientで生成しているよう。
ここでProxyの判断をして、セットすれば宜しいんじゃないかと。
で、Apache CamelのSpring Boot Starterを使ったのですが、HttpComponentをセットアップする為のBean定義（"http-component", "https-component"）が用意されていたので、これを上書きする感じで独自Configを作成してみた。（元々ルートのパラメータでhttpClientConfigurerで指定できるもののデフォルトを上書いているイメージ）

※ちなみに、今回利用しているApache Camel、およびそのStarterは、全て2.19.4でのお話。また、camel-http使う前提。

```java
@Bean(name = { "http-component", "https-component" })
public HttpComponent configureHttpComponent(
  final CamelContext camelContext,
  final HttpComponentConfiguration configuration) throws Exception {
	final HttpComponent component = new HttpComponent(HttpEndPointExt.class);
	// 以下省略
  }
```
はじめは、HttpComponentのコンストラクタで、HttpEndPointの拡張クラスセットすりゃ良いんじゃ？と思っていたのですが、実はこれは使わておらず、普通にHttpEndPointをnewしておりました。。。orz

なので、そのメソッドも拡張して、
```java
@Bean(name = { "http-component", "https-component" })
public HttpComponent configureHttpComponent(
  final CamelContext camelContext,
  final HttpComponentConfiguration configuration) throws Exception {
    final HttpComponent component = new HttpComponent(HttpEndPointExt.class) {
      @Override
      protected HttpEndpoint createHttpEndpoint(
        final String uri, final HttpComponent component,
        final HttpClientParams clientParams,
        final HttpConnectionManager connectionManager,
        final HttpClientConfigurer configurer) throws URISyntaxException {
          return new HttpEndPointExt(uri, component, clientParams, connectionManager, configurer);
    }
    // 以下省略
  }
};
```
とした。これでとりあえずProxyセットするフックは準備できた。
で、肝心のHttpEndPointでHttpClient生成時の拡張は、以下のような感じ。
```java
@Override
public HttpClient createHttpClient() {
  final HttpClient httpClient = super.createHttpClient();
  if(StringUtils.isNotBlank(proxyHost) && proxyPort != null) {
    if(StringUtils.isBlank(httpClient.getHostConfiguration().getProxyHost())) {
      if(needProxy(getEndpointUri())) {
        // LOG INFO
        logger.info("Setting proxy for EndPoint : {}", getEndpointUri());
        httpClient.getHostConfiguration().setProxy(proxyHost, proxyPort);
      }
    } else {
      // LOG INFO
      logger.info("EndPoint '{}' already set proxy. proxyHost = {}, proxyPort = {}"
        , getEndpointUri()
        , httpClient.getHostConfiguration().getProxyHost()
        , httpClient.getHostConfiguration().getProxyPort());
    }
  }
  return httpClient;
}
```
一応、パラメータとして明示的にproxyHost、proxyPortをセットされた場合は、これが最強と判断することで、大人の対応をしてみた。

あれ、そういえばGeoCoderの話忘れてた。実はGeoCoderはこれ使ってません。m9(^Д^)ﾌﾟｷﾞｬｰ

GeoCoderComponentで、GeoCodeEndPointを生成しています。うーん、なんだか。
ま、ほぼ同じことすれば良いので、同じSpring BootのConfigクラスにしてしまって、実装しました。

## ちなみに。。。

こんな話、Apache HttpClient（今は、HttpComponent？）では日常ちゃめしごとであるはずで、実際HttpClientBuilderってのがあって、RoutePlannerっていうのでいけるようなのですが。。。camel-httpでは少なくとも使っていないように思います（ちゃんと読んでないだけ！？）。

あと、やり終えたあたりで、「camel-http4あるってよ」とGoogleさんが教えてくれましたが、とりあえずガン無視して本日はここまで。
結局、Apache Camel弄るはずが、関係無い話ばかりになっちゃったのは、スキル不足ゆえ。残念っ。
