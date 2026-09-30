# MaDKit documentation site

The Markdown files in this directory are the authored source for the MaDKit GitHub Pages site.

## Local preview

The canonical CI build uses the repository workflow. If a compatible Jekyll installation is available locally, preview from the repository root with:

```bash
jekyll serve --source docs
```

The site assembly also copies generated core Javadoc to `api/latest/`. Do not commit rendered output or files from `build/`.

## Content rules

- Keep pages concise and link to subproject READMEs for detailed technical material.
- Verify commands against the current Gradle build before publishing them.
- Mark legacy v5 material clearly.
- Keep headings, link text, alt text, and navigation meaningful for keyboard and screen-reader users.
