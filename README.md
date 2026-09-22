![Waylight Banner](assets/waylight-banner.png)

![Fabric: 1.21.11–26.3](https://img.shields.io/badge/Fabric-1.21.11%E2%80%9326.3-DBB78A)

**A client-side virtual lantern mod for Fabric**, giving your player configurable vanilla lantern carry visuals with localized dynamic light and no inventory item requirement.

> Waylight is client-side only and does not modify server-side gameplay.

Waylight includes:

- Vanilla lantern and soul lantern rendering
- Carry positions: right hip, left hip, and left hand
- First-person and third-person lantern behavior
- Procedural carry motion with configurable intensity
- Light emission localized to the lantern rig via LambDynamicLights
- Optional auto-equip in darkness and auto-unequip in brightness
- Underwater extinguish behavior
- Full Mod Menu + YACL configuration UI

If you encounter any issues, please [report them here](https://github.com/soradotwav/waylight/issues).

### Controls

- `G`: Toggle lantern on/off

### Config Highlights

- Lantern type: `Lantern` or `Soul Lantern`
- Lantern position: `Right Hip`, `Left Hip`, or `Left Hand`
- First-person light toggle
- First-person hand motion: `Physics` or `Static`
- Auto-light behavior with threshold (`0-15`)
- Motion intensity slider (`25%-200%`)
- Debug anchor gizmo toggle

Config is saved at `config/waylight.json`.

### Requirements

Use the Waylight jar and dependency releases matching your Minecraft version.

| Minecraft | Java | Fabric Loader | LambDynamicLights |
| --- | --- | --- | --- |
| 1.21.11 | 21+ | 0.18.4+ | 4.9.1+ for 1.21.11 |
| 26.1–26.1.2 | 25+ | 0.18.4+ | 4.10.0+ for 26.1 |
| 26.2 | 25+ | 0.19.5+ | 4.12.4+ for 26.2 |
| 26.3 | 25+ | 0.19.5+ | 4.13.0+ for 26.3 |

- Fabric API for your Minecraft version
- YACL is bundled
- Optional: Mod Menu

### Behavior Notes

- Waylight is virtual and does not require a real lantern item.
- In left-hand mode, the lantern suppresses if you are swimming or your offhand is occupied.
- With `Extinguish Underwater` enabled, the lantern can remain visible while light output is disabled.
- In first-person hip modes, the model is hidden by default; `First-Person Light` controls whether light remains active.

### License

`LGPL-3.0-or-later` (see [LICENSE](LICENSE)).
