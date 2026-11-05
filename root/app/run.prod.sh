#!/bin/sh

# Run the compiled Java client.
java \
  --enable-final-field-mutation=ALL-UNNAMED \
  -cp "/opt/tt/*:/app/bin" \
  tt.StoryGraphAgentMain
