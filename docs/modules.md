---
layout: page
title: Modules and Integrations
permalink: /modules/
---

The repository is a multi-project Gradle build. The core module is the stable starting point; the other projects demonstrate focused capabilities and integrations.

## Core and simulation projects

- [`MaDKit`](https://github.com/fmichel/MaDKit/tree/main/MaDKit) — core Java API and kernel.
- [`MDK-simu-template`](https://github.com/fmichel/MaDKit/tree/main/MDK-simu-template) — minimal simulation template.
- [`MDK-simu-integration-common`](https://github.com/fmichel/MaDKit/tree/main/MDK-simu-integration-common) — shared simulation integration support.
- [`MDK-simu-integration-tests`](https://github.com/fmichel/MaDKit/tree/main/MDK-simu-integration-tests) — integration validation.
- [`MDK-simu-integration-ui`](https://github.com/fmichel/MaDKit/tree/main/MDK-simu-integration-ui) — simulation UI integration support.

## Optional demonstrations

The workspace also contains bees, market organization, chart-fx, and TurtleKit demonstrations. Their individual READMEs are the authority for setup requirements and platform-specific behavior.

[Read the generated core API documentation]({{ '/api/latest/' | relative_url }}).
