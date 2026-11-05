#!/bin/sh

# Run the compiled Java client with the self-signed trust store.
java \
  -Djavax.net.ssl.trustStore="/etc/tt/certs/tt-truststore.p12" \
  -Djavax.net.ssl.trustStorePassword="changeit" \
  --enable-final-field-mutation=ALL-UNNAMED \
  -cp "/opt/tt/*:/app/bin" \
  tt.StoryGraphAgentMain
