# AutoTranslator
A Minecraft mod that uses advanced AI to automatically translate untranslated content.


## Project Discontinued

This project has been discontinued due to fundamental limitations in Minecraft's text handling system:

1. Core Minecraft features like recipe books and creative inventory require building complete text indices, which would trigger translation calls for all text content at once.

2. Popular mods like JEI and EMI also need to index all available text content for their functionality.

3. This pattern of requiring complete text indices is both legitimate and unpredictable - we cannot anticipate all future cases where bulk text access might be needed.

These limitations make it technically unfeasible to implement real-time translation when text is accessed, as the system would be overwhelmed by mass translation requests during index building operations.

The original goal was to provide seamless translation of untranslated content, but Minecraft's architecture makes this approach impractical to implement reliably and efficiently.