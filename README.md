# Bringing Your Canvas Creations to Life with Jetpack Compose

The slide deck for my talk, built as a Compose Multiplatform Desktop app. Every animation on the slides is live Compose code, and the code shown next to it is the code that runs.

What the talk covers:

- Gradient text with `CompositingStrategy.Offscreen`, `drawWithCache` and `BlendMode.SrcAtop`
- Mesh gradients with `MeshGradientPainter`
- `BlendMode.DstIn` for fades and masks
- Blend-mode "dance" bubbles, the TILT boost bubble and its glowing ring
- Particles with [ParticleEmitter](https://github.com/PiotrPrus/ParticleEmitter)
- A fortune wheel with motion blur
- Morphing blobs with [KBlobs](https://github.com/PiotrPrus/KBlobs) ([interactive explainer](https://piotrprus.github.io/KBlobs/demo/))

## Run it

```
./gradlew run
```

Use the arrow keys or space to move through the slides.

Other tasks:

- `./gradlew renderSlides` renders every slide step to PNG in `build/slides` (pass `-Pout=<dir>` or `-Psection=<name>` to narrow it down).
- `./gradlew packageDmg` builds a macOS app.

## Author

Piotr Prus, [@TILT](https://tilt.app)
