# Fine Tune GPT3 for Code

Dependencies:
* [GPT-3 from OpenAI](https://github.com/openai/openai-node)
* [yargs for command-line arguments](https://github.com/yargs/yargs)
* [TypeScript for Compiler API](https://github.com/microsoft/TypeScript/wiki/Using-the-Compiler-API)
* [dotenv for loading environment variables](https://github.com/motdotla/dotenv)

## Install and Setup

```shell
npm install
```

You can store the api key in a `.env` file and control debug logging with the `DEBUG` environment variable.

```
OPENAI_API_KEY="YOUR_OPENAI_API_KEY_GOES_HERE"
DEBUG="true"
```

### Dataset and TypeScript/JavaScript Code Parser
The dataset is in [`./dataset.csv`](./dataset.csv).

To produce more completions for the dataset, you can use the parser which will parse TypeScript and JavaScript:

```shell
# Parsing a directory of source code files
node index.js parse ./input

# Parsing the source code of finetune-gpt3-for-code
node index.js parse ./index.js

# Parsing individual source code files
node index.js parse /path/to/typescript/file.ts
node index.js parse /path/to/javascript/file.js
```

It will produce the following output that can be used to extend the dataset:

```
prompt,completion
for loop,"for (var i = 0; i < 10; i ++) {"
for loop,"for (var i = 0; i < 10; i ++) {
  console.log(i);
}"
print i,"console.log(i)"
function for printHelloWorld with one argument,"function printHelloWorld(name) {"
function for printHelloWorld with one argument,"function printHelloWorld(name) {
  console.log(`Hello ${name}`);
}"
```

## Running the Code

Upload the dataset and create the fine-tuned model:

```shell
node index.js
```

Alternative way to upload the dataset:

```shell
node index.js upload
```

### Listing the status of the fine-tuned models

List the status of the fine-tuning until the `fine_tune_model` field is no longer null

```shell
node index.js list
```

### Creating a completion

Use the fine tune id as the model when it is ready

```shell
node index.js generate model-finetune-id prompt
```

### Example Session

```
$ node index.js
Fine tune id: id-123

$ node index.js list
ft-nlBqfegq5AP1QkfLKOCvuz6u succeeded curie:ft-personal-2023-01-07-06-54-13
ft-l8B3FiHivRuMWxj8U1YvQKq2 running null

$ node index.js generate curie:ft-personal-2023-01-07-06-54-13 "define apply effect"
const yEff = (value) => ({ done: false, value })

$ node index.js generate curie:ft-personal-2023-01-07-06-54-13 "test api call saga"
function* fetchProducts() {
  const products = yield call(Api

$ node index.js generate curie:ft-personal-2023-01-07-06-54-13 "test api call saga"
assert.deeplyEqual(
  iterator.next().value,

$ node index.js list
ft-nlBqfegq5AP1QkfLKOCvuz6u succeeded curie:ft-personal-2023-01-07-06-54-13
ft-l8B3FiHivRuMWxj8U1YvQKq2 succeeded davinci:ft-personal-2023-01-07-07-00-01

$ node index.js generate davinci:ft-personal-2023-01-07-07-00-01 "define apply effect"
const arrayOfValues = { value: 5 }
  const expectedEffect =

$ node index.js generate davinci:ft-personal-2023-01-07-07-00-01 "test api call saga"
assert.deepEqual(
import { cps } from 'redux

$ node index.js generate davinci:ft-personal-2023-01-07-07-00-01 "test api call saga"
assert.deepEqual(
  iterator.next(assert.isNot
```

## Running the code using Docker

```shell
# npm run-script docker:build
docker build -t finetune-gpt3-for-code .

# npm run-script docker:run
docker run -it --rm --log-driver none --env-file .env -v $(pwd)/data:/data finetune-gpt3-for-code
```
