package dev.piotrprus.canvascreations.presentation.slides

import dev.piotrprus.canvascreations.presentation.deck.Slide

/** Deck sections in talk order; keys are used by `./gradlew renderSlides -Psection=…`. */
fun deckSections(): Map<String, List<Slide>> = linkedMapOf(
    "intro" to introSlides,
    "mindset" to mindsetSlides,
    "basics" to basicsSlides,
    "animation" to animationSlides,
    "mesh" to meshSlides,
    "dstin" to dstInSlides,
    "dance" to danceSlides,
    "boost" to boostSlides,
    "particles" to particleSlides,
    "wheel" to fortuneWheelSlides,
    "blobs" to blobSlides,
    "end" to endSlides,
)

fun deckSlides(): List<Slide> = deckSections().values.flatten()
