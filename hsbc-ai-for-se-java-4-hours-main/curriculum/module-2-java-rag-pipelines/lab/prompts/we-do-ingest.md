# Prompt (bonus): explain the chunker, the embedder and the index

I am building the ingestion half of a small retrieval system in Java 21 (no libraries). The specification is in the Javadoc of the
attached Chunker.java, Embedder.java and InMemoryIndex.java, and the tests are in the attached ChunkerTest, EmbedderTest and
InMemoryIndexTest.

These classes are already finished in this lab. Do NOT change any code. Instead, explain how Chunker.chunk(), Embedder.embed(), InMemoryIndex.addDocument() and InMemoryIndex.search() work, in plain English, one short paragraph each.
Do not run any terminal commands.

Then tell me what happens at the very end of the text and why that is the risky part.
And tell me why this is not a real embedding and what a production system would use instead.
