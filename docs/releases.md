---
layout: page
title: Releases and Migration
permalink: /releases/
---

## Current line

MaDKit v6 is published through [Maven Central](https://central.sonatype.com/artifact/io.github.fmichel/madkit). The repository's release tags and build files are the authority for exact version numbers and compatibility requirements.

Generated API documentation for the current site publication is available at [API latest](api/latest/).

## Migrating from v5

The current site focuses on MaDKit v6. Older tutorials and pages may still be useful for concepts, but v5 instructions should not be assumed to match the v6 module layout, Java requirements, or APIs.

When a v6 migration guide is expanded, it should document concrete differences and link to the relevant release notes rather than silently mixing generations.

## Documentation versioning

The first publication uses `api/latest/` to keep onboarding simple. Future releases may add `api/<version>/` while keeping `api/latest/` as the current stable entry point.
