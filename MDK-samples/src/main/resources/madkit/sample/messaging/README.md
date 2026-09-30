# Messaging samples

These examples cover point-to-point messages, request/reply workflows,
broadcasts, and enum-based message dispatch. Start with `MessagingLauncher`
or one of the request/reply demos to see agents exchange messages through roles.

`RequestReplyDemo` is the smallest request/reply example: it creates a requester
and responder and waits indefinitely for the expected `Pong!` reply. It has no
missing-recipient or timeout scenario.

`RequestReplyTimeoutDemo` builds on that exchange with two bounded failure
scenarios: a missing role that returns immediately and a silent recipient that
returns after its finite timeout. It illustrates why timeout-aware callers can
remain responsive when a peer is unavailable.

Launchable classes: `MessagingLauncher`, `RequestReplyDemo`,
`RequestReplyTimeoutDemo`, `BroadcastDemo`, and `EnumDispatchDemo`.

[View the source code on GitHub](https://github.com/fmichel/MaDKit/tree/main/MDK-samples/src/main/java/madkit/sample/messaging)
