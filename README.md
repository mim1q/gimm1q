# Gimm1q

Mim1q's common utilities library for Minecraft Fabric mods.  
The name is a play on the word "gimmick" and my nickname "Mim1q" :grin:

## Table of contents

- [Goals](#goals)
- [Implemented features](#implemented-features)
  - [Screen shaking effects](#screen-shaking-effects)
  - [Block and entity highlighting](#block-and-entity-highlighting)
  - [Animation and interpolation utilities](#animation-and-interpolation-utilities)
  - [Handheld item model switching](#handheld-item-model-registering)
- [Possible future features](#possible-future-features)
- [Usage](#usage)
- [License](#license)

## Goals

- Provide a set of utilities that could be useful in most content-adding mods;
- Keep the size small to enable inclusion in any mod;
- Keep the code simple, clean, and well-documented with examples.

## Implemented features

The codebase is thoroughly documented, so you can check out the source code (and the Testmod) for more details about
any of the features.

### Screen shaking effects

Players' screens can be shaken during any event with a simple server-side function call.  

```java
public void someExplosionEvent(ServerWorld world, Vec3d explosionPos) {
    // ...
    // Shake the screen of all players in a 10-block radius
    ScreenShakeUtils.shakeAround(
        world,         // The world where the shake happens
        explosionPos,  // The position of the shake's center
        1.0f,          // The maximum intensity of the shake
        20,            // The duration of the shake in ticks
        5.0f,          // The inner radius of the shake in blocks (max intensity)
        10.0f,         // The outer radius of the shake in blocks (0 intensity)
        "explosion"    // The "modifier" of the shake
    );
    // ...
}
```

The last argument is a "modifier" that can be set during mod initialization to allow players to multiply a screen shake 
event's intensity using their config files or any other way, for example:

```java
@Override
public void onInitialize() {
    // load the config etc...
    ScreenShakeModifiers.setModifier("explosion", CONFIG.explosionScreenShakeIntensity);
}
```

### Block and entity highlighting

You can register a callback to a Highlight Event that will be called every frame and will let you highlight blocks,
entities or arbitrary boxes in the world using your chosen color and logic.

An example of highlighting an entity with a black outline and transparent fill color:

```java
@Override
public void onInitializeClient() {
    HighlightDrawerCallback.EVENT.register((drawer, context) -> {
        var entity = /* select the entity to highlight */;
        drawer.highlightEntity(
            entity,     // The entity to highlight
            0x00000000, // The color of the highlight (0xAARRGGBB)
            0xFF000000  // The color of the outline (0xAARRGGBB)
        );
    });
}
```

### Animation and interpolation utilities

The library provides a set of utilities for creating very simple animations and interpolating values.

The `dev.mim1q.interpolation.Easing` class provides a set of common easing functions (with more likely to come!).

The `dev.mim1q.interpolation.AnimatedProperty` class lets you create a single-float-value property that can be changed
over time using a specified easing function and duration.

### Handheld item model registering

Gimm1q allows you to register items that will have a different model when held in the player's hand from the one they
have in the GUI - sort of like the vanilla Spyglass and Trident items.

```java
@Override
public void onInitializeClient() {
    HandheldItemModelRegistry.getInstance().register(
        // The item to register
        SOME_ITEM,                                    
        // The GUI model identifier (without the `item/` prefix!)
        new Identifier("modid", "gui/some_item"),     
        // The handheld model identifier (as above)
        new Identifier("modid", "handheld/some_item") 
    );
}
```

This will load the declared models for you and automatically render the correct one depending on the context.

## Possible future features
(may or may not be implemented in the future)

- Easier way to create particles with a color parameter
- Simple camera utils (for cutscenes etc.)

## Usage

Gimm1q uses JitPack for distribution. (That might change in the future, though)  
To use it in your mod, add the following to your `build.gradle.kts` file's dependencies and repositories sections:

```kotlin
repositories {
    maven("https://jitpack.io") // JitPack repository for Gimm1q
}

dependencies {
    modImplementation(include("com.github.mim1q:gimm1q:${Versions.GIMM1Q}")!!)
}
```
Replace `${Versions.GIMM1Q}` with the version you want to use. The versions are available on 
[JitPack](https://jitpack.io/#mim1q/gimm1q). 

Alternatively you can head to the 
[releases page](https://github.com/mim1q/gimm1q/releases), download the jar manually and figure out how to include it
in your project from that point :wink:

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
