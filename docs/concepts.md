---
layout: page
title: Core Concepts
permalink: /concepts/
---

## Agents, groups, and roles

MaDKit uses the **Agent/Group/Role (AGR)** model:

- An **agent** is an autonomous participant whose internal architecture is yours to define.
- A **group** is an organizational space shared by agents.
- A **role** is a capability or responsibility an agent plays inside a group.

This separation lets applications express organization and communication without forcing every agent into one behavior model.

## Agent lifecycle

A typical agent uses three lifecycle stages:

- `onActivation()` prepares the agent and its organization memberships.
- `onLive()` performs its work and communication.
- `onEnd()` releases resources and performs cleanup.

The lifecycle is a starting point, not a restriction. Agents can be threaded, reactive, GUI-backed, or integrated with a custom simulation engine.

## Messaging

Agents communicate through MaDKit's organization and messaging APIs. Samples cover point-to-point messages, replies, broadcasts, logging, and role discovery.

## Simulation authoring

MaDKit provides reusable simulation settings and engine components while leaving the model-specific choices to the application. Begin with the simulation template, then move to the complete bees or market organization applications when you need a larger reference.
