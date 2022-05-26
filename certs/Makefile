# Copyright (c) 2022, ninckblokje
# All rights reserved.

# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:

# * Redistributions of source code must retain the above copyright notice, this
#   list of conditions and the following disclaimer.

# * Redistributions in binary form must reproduce the above copyright notice,
#   this list of conditions and the following disclaimer in the documentation
#   and/or other materials provided with the distribution.

# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
# AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
# IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
# DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
# FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
# DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
# SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
# CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
# OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
# OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

clean:
	rm *.pem *.pfx *.cer

ca:
	openssl genrsa 2048 > ca-key.pem
	cat input-ca.txt | openssl req -new -x509 -nodes -days 365 -key ca-key.pem -out ca-cert.pem
	openssl x509 -in ca-cert.pem -out ca-cert.cer -outform DER

server:
	cat input-server.txt | openssl req -newkey rsa:2048 -nodes -days 365 -keyout server-key.pem -out server-req.pem
	openssl x509 -req -days 365 -set_serial 01 -in server-req.pem -out server-cert.pem -CA ca-cert.pem -CAkey ca-key.pem
	cat ca-cert.pem > server-cert-bundle.pem
	cat server-cert.pem >> server-cert-bundle.pem
	openssl pkcs12 -inkey server-key.pem -in server-cert-bundle.pem -export -out server.pfx -password pass:Dummy_123
	openssl x509 -in server-cert.pem -out server-cert.cer -outform DER

client:
	cat input-client.txt | openssl req -newkey rsa:2048 -nodes -days 365 -keyout client-key.pem -out client-req.pem
	openssl x509 -req -days 365 -set_serial 01 -in client-req.pem -out client-cert.pem -CA ca-cert.pem -CAkey ca-key.pem
	cat ca-cert.pem > client-cert-bundle.pem
	cat client-cert.pem >> client-cert-bundle.pem
	openssl pkcs12 -inkey client-key.pem -in client-cert-bundle.pem -export -out client.pfx -password pass:Dummy_123
	openssl x509 -in client-cert.pem -out client-cert.cer -outform DER

verify:
	openssl verify -CAfile ca-cert.pem ca-cert.pem server-cert.pem
	openssl verify -CAfile ca-cert.pem ca-cert.pem client-cert.pem
