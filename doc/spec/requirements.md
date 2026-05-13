# Requirements

## Go to Definition: Coverage Tag to Spec Item

`req~goto-definition-tag-to-spec~1`

When the user invokes Go to Definition with the cursor on a coverage tag, the server returns the location of the referenced specification item.

Covers: feat~goto-definition~1

Needs: impl, utest

## Go to Definition: Spec Item to Covering Tags

`req~goto-definition-spec-to-tags~1`

When the user invokes Go to Definition with the cursor on a specification item ID, the server returns the locations of all coverage tags that cover that item.

Covers: feat~goto-definition~1

Needs: impl, utest

## Find References Returns All Covering Tags

`req~find-references-covering-tags~1`

When the user invokes Find References on a specification item, the server returns the file locations of all coverage tags in the workspace that reference that item's ID.

Covers: feat~find-references~1

Needs: impl, utest

## Hover Shows Title and Description

`req~hover-title-and-description~1`

When the user hovers over a coverage tag, the server returns a Markdown-formatted response containing the specification item's title and description.

Covers: feat~hover-documentation~1

Needs: impl, utest

## Diagnostic for Outdated Version

`req~diagnostic-outdated-version~1`

The server emits a diagnostic warning for every coverage tag whose referenced revision does not match the current revision of the specification item.

Covers: feat~quickfix-outdated-version~1

Needs: impl, utest

## Quick Fix Updates Version Number

`req~quickfix-updates-version~1`

For each diagnostic produced by `req~diagnostic-outdated-version~1`, the server provides a code action that replaces the outdated revision number in the coverage tag with the current revision of the specification item.

Covers: feat~quickfix-outdated-version~1

Needs: impl, utest

## Workspace Indexing on Startup

`req~index-on-startup~1`

On receiving the `initialized` notification, the server imports all OFT-traceable files in the workspace and builds an internal index.

Needs: impl, utest

## Index Refresh on File Save

`req~index-refresh-on-save~1`

On receiving `textDocument/didSave` or `workspace/didChangeWatchedFiles`, the server refreshes the index within 300 ms.

Needs: impl, utest
