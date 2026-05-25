# Redstone Pen Continuation

Redstone Pen Continuation is an unofficial continuation of Redstone Pen, a Minecraft Java Edition mod that adds tools for drawing thin redstone tracks, relays, and programmable redstone logic.

This fork exists to keep the mod available after the original project reached end of life.

## What It Adds

Redstone Pen adds "one pen to draw them all" and helps with compact redstone handling. It is inspired by Redstone Paste, but has its own track, relay, and logic-control features.

Main features:

- Redstone Quill and Redstone Pen items for drawing thin redstone tracks
- multiple independent redstone tracks in one block space
- track inspection while sneaking
- explicit connectors for deciding whether a track powers the block underneath it
- redstone relays for compact signal routing
- redstone logic control documentation and examples

## Downloads

Use the approved Modrinth or CurseForge project page when available. Development builds may also be attached to GitHub releases for this repository.

Original project pages:

- CurseForge: https://www.curseforge.com/minecraft/mc-mods/redstone-pen/files
- Modrinth: https://modrinth.com/mod/redstonepen

## Installation

1. Install the loader required by the release file.
2. Put the Redstone Pen Continuation jar in your `mods` folder.
3. Launch the game.
4. Check recipe viewers or the original documentation for item recipes and examples.

## Redstone Quill and Pen

The Redstone Quill uses redstone dust directly from your inventory.

The Redstone Pen stores redstone in the item and can be refilled in the crafting grid with redstone dust or redstone blocks.

Both tools can draw or remove thin redstone tracks. They can also inspect the current signal of a block, track, wire, or device when you sneak while holding the tool and look at the target.

## Track Connectors

Pen tracks normally do not connect to the block they are drawn on. This makes compact wiring easier because separate tracks can pass through the same area without accidentally powering nearby blocks.

To make a track power the block underneath it, add an explicit connector by clicking the center of the track with the pen.

## Redstone Relays

Relays move built-in redstone torches to re-power signals back to strength 15. They can be placed on solid faces in all directions. Output is at the front, and inputs are accepted from the other sides.

Relay types include:

- Redstone Relay
- Inverted Redstone Relay
- Bi-Stable Redstone Relay
- Pulse Redstone Relay

## Documentation

Additional documentation and examples are in the `documentation` folder.

## Reporting Issues

Open issues on this continuation repository for bugs in this fork.

Include:

- Minecraft version
- loader and loader version
- Redstone Pen Continuation version
- a short description of the circuit or item behavior
- screenshots or a small world/example when useful
- `latest.log` or the crash report

## Credits

Redstone Pen was originally created by WilE. This continuation keeps the mod available for newer environments while preserving the original project credits and license terms.
