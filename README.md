# AnimeAI Server

This repository contains a Flask server providing APIs for anime-style character interaction.

## New Endpoint

### `POST /api/generate-character-image`
Generates an anime character image using OpenAI's `gpt-image-1` model. The request body must include a `prompt` describing the desired character. You can optionally provide a base64 `image` to influence generation and set `animate` to `true` to receive animation frames.

Example request body:
```json
{
  "prompt": "a cheerful anime girl with pink hair",
  "size": "1024x1024",
  "image": "<base64 data>",
  "animate": true
}
```
The response contains either an `image` field or a list of animation `frames`:
```json
{ "frames": ["<frame1>", "<frame2>"] }
```

Set the `OPENAI_API_KEY` environment variable before running the server.

