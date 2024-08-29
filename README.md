# http-receiver

`http-receiver` can be used to test an HTTP connection and will print all the headers and the body. It is possible to
configure the response using environment variables.

`http-receiver` is available as a Docker image: `ninckblokje/http-receiver`

## Configuration

It is possible to configure `http-receiver` using environment variables specified in the table below.

| Key                                           | Default value          | Description                                        |
|-----------------------------------------------|------------------------|----------------------------------------------------|
| HTTP_RECEIVER_PORT                            | 8888                   | HTTP listen port                                   |
| HTTP_RECEIVER_RESPONSE_RETURN_REQUEST_DETAILS | true                   | Return information from the request                |
| HTTP_RECEIVER_RESPONSE_STATUS_CODE            | 200                    | HTTP status code                                   |
| HTTP_RECEIVER_RESPONSE_STATUS_MESSAGE         |                        | HTTP status message                                |
| HTTP_RECEIVER_RESPONSE_STATUS_CONTENT_TYPE    | application/json       | Content type of response                           |
| HTTP_RECEIVER_RESPONSE_CONTENT                | *see response message* | Response                                           |
| HTTP_RECEIVER_PFX_STORE_PATH                  |                        | Path to PFX file for TLS, if empty then plain HTTP |
| HTTP_RECEIVER_PFX_STORE_PASSWORD              |                        | Password for the PFX file                          |
| HTTP_RECEIVER_TLS_ENABLED_PROTOCOLS           | TLSv1.2,TLSv1.3        | Which TLS protocols should be enabled              |
| HTTP_RECEIVER_LOG_AUTHORIZATION_HEADER        | false                  | To log the authorization header (true of false)    |

## Response message

Example JSON:

````json
{
  "remoteAddress": "127.0.0.1:64090",
  "absoluteURI": "http://server.dummy/dummy",
  "httpMethod": "GET",
  "receivedHeaders": [
    {
      "name": "Authorization",
      "value": "***"
    },
    {
      "name": "Host",
      "value": "server.dummy"
    },
    {
      "name": "User-Agent",
      "value": "IntelliJ HTTP Client/IntelliJ IDEA 2024.1.4"
    },
    {
      "name": "Accept-Encoding",
      "value": "br, deflate, gzip, x-gzip"
    },
    {
      "name": "Accept",
      "value": "*/*"
    },
    {
      "name": "content-length",
      "value": "0"
    }
  ],
  "receivedBody": ""
}
````
