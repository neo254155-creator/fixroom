# Evidence from the live Switch/Discord workflow

These files were captured from the actual FIX-001 workflow, separate from the automated rehearsal in `rehearsal-evidence/`.

| File | Evidence |
|---|---|
| [before.log](before.log) | Maven/JUnit reproduction: 8 checkout tests, 3 assertion failures |
| [after.log](after.log) | Maven/JUnit verification: all 8 checkout tests passed |
| [full-tests.log](full-tests.log) | Final project build: all 17 tests passed |
| [resolution.md](resolution.md) | Proposed correction, approval record and timestamped incident timeline |
| [incidents.json](incidents.json) | Saved incident state with original source/test hashes and before/after evidence |

Original local paths and timestamps remain in the logs and JSON to preserve the record. Their absolute paths refer to the development machine, not to files a reviewer must create. The recorded approval name is supplied by the caller; the local API does not independently authenticate Discord identities. Pair this evidence with the Discord walkthrough video for the complete human-agent interaction.
