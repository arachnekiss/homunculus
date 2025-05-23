# AnimeAI Server

This repository contains a Flask server providing APIs for anime-style character interaction.

## New Endpoint

### `POST /api/generate-character-image`
Generates an anime character image using OpenAI's `gpt-image-1` model.
The request body may contain a text `prompt` and/or an `image` (base64). When an
image is supplied the engine converts it into an animated-style avatar. Set
`animate` to `true` to receive multiple frames for simple animation.

Example request body:
```json
{
  "prompt": "a cheerful anime girl with pink hair",
  "image": "<base64 image>",
  "size": "1024x1024",
  "animate": false
}
```
The response contains either a base64 encoded image or a list of frames:
```json
{ "image": "<base64 data>" }
// or
{ "animation": ["<frame1>", "<frame2>"] }
```

Set the `OPENAI_API_KEY` environment variable before running the server.

