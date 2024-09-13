# http-receiver

`http-receiver` can be used to test an HTTP connection and will print all the headers and the body. It is possible to
configure the response using environment variables.

`http-receiver` is available as a Docker image: `ninckblokje/http-receiver`

## Configuration

It is possible to configure `http-receiver` using environment variables specified in the table below.

| Key                                           | Default value          | Description                                        |
|-----------------------------------------------|------------------------|----------------------------------------------------|
| HTTP_RECEIVER_LOG_AUTHORIZATION_HEADER        | false                  | To log the authorization header (true of false)    |

## Response message

Example JSON:

````json
{
  "absoluteURI": "/dummy",
  "cipher": "null",
  "httpMethod": "GET",
  "isSecure": false,
  "receivedHeaders": {
    "Authorization": "***",
    "Accept": "*/*",
    "User-Agent": "IntelliJ HTTP Client/IntelliJ IDEA 2024.2.1",
    "Host": "server.dummy",
    "Accept-Encoding": "br, deflate, gzip, x-gzip",
    "Content-Length": "0"
  },
  "remoteAddress": "127.0.0.1",
  "tlsVersion": "null"
}
````
