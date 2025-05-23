# AnimeAI Server

This repository contains a Flask server and demo web interface for interacting with animated AI characters.

## `/api/generate-character-image`
Generates an anime character image using OpenAI's **gpt-image-1** model. The request accepts a text `prompt`, an optional base64 encoded `image` to guide generation, and an `animate` flag. When `animate` is true the endpoint returns multiple frames that can be displayed as a simple animation.

Example request body:
```json
{
  "prompt": "cheerful anime girl with pink hair",
  "image": "<optional base64 input>",
  "animate": true,
  "size": "1024x1024"
}
```

Example response when `animate` is false:
```json
{ "image": "<base64 data>" }
```
When `animate` is true:
```json
{ "frames": ["<frame1>", "<frame2>", "<frame3>"] }
```

Set the `OPENAI_API_KEY` environment variable before running the server.
