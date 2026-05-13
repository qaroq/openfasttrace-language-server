# Features

## Go to Definition

`feat~goto-definition~1`

The language server enables navigation from a coverage tag in source code to the corresponding specification item in Markdown, and from a specification item to all its covering tags.

Needs: req

## Find References

`feat~find-references~1`

The language server lists all coverage tags in the workspace that cover a given specification item.

Needs: req

## Hover Documentation

`feat~hover-documentation~1`

The language server displays the title and description of a specification item as a hover tooltip when the cursor is positioned over a coverage tag.

Needs: req

## Quick Fix: Outdated Version

`feat~quickfix-outdated-version~1`

The language server detects coverage tags that reference an outdated revision of a specification item and offers a quick fix to update the version number to the current revision.

Needs: req
