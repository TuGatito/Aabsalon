# Aabsalon

Aabsalon is a small yet ambitious Entity-Component-System (ECS) library written in Kotlin. Designed to simplify game development, it provides a structured way to manage game objects and their behavior. While still in its early stages, Aabsalon is continuously evolving to incorporate more features and optimizations.

## Features

- **Entity-Component-System Architecture:**
  - Manage entities using a simple actor-based system.
  - Define reusable components (attributes) to represent the state of your entities.
  - Add behaviors (systems) to manipulate entities and their components across different phases.

- **Lifecycle Phases:**
  - Easily define behaviors for initialization (`Init`), updates (`PreUpdate`, `Update`, `PostUpdate`), rendering (`Draw`), and cleanup (`End`).

- **Attribute Filtering:**
  - Retrieve actors dynamically based on the components they possess.

- **State Persistence:**
  - Save and load the entire state of the ECS system in JSON format.

- **Threaded Behaviors:**
  - Optionally execute behaviors in parallel for improved performance.

## Inspiration

Aabsalon draws inspiration from several popular libraries and frameworks:

- **[Bevy](https://bevyengine.org):** A powerful ECS game engine in Rust, inspiring the core lifecycle phases and design principles.
- **[Godot](https://godotengine.org):** Planned features will take inspiration from Godot's node-based architecture to provide a hybrid system.
- **[KaPlay (formerly Kaboom.js)](https://kaboomjs.com):** Future updates aim to include playful and intuitive component systems inspired by KaPlay.

## Roadmap

The library will be expanded with the following features:

1. **Optimizations:**
   - Improve performance for large-scale games with many entities and components.

2. **Utilities:**
   - Add helpers to simplify common tasks like movement, collisions, and animations.

3. **Node System:**
   - Integrate a node-based structure inspired by Godot for hierarchical entity management.

4. **Scene Management:**
   - Enable developers to switch between different scenes effortlessly.

## Getting Started

1. **Add Aabsalon to Your Project:**
   ```kotlin
   // TODO: Add dependency information when available
   ```

2. **Basic Usage:**
   ```kotlin
   val ecs = Aabsalon()

   // Create an actor with attributes
   val player = ecs.createActor(arrayOf(Position(0, 0), Health(100)))

   // Add behaviors
   ecs.addBehavior(Phase.Update) { manager ->
       manager.getActorsWith(Position::class).forEach { actor ->
           val position = manager.getAttribute(actor, Position::class)!!
           position.x += 1 // Move entity to the right
       }
   }

   // Run the ECS lifecycle
   ecs.init()
   ecs.update()
   ecs.draw()
   ecs.end()
   ```

3. **Save and Load State:**
   ```kotlin
   // Save state to a file
   ecs.saveToFile(File("state.json"))

   // Load state from a file
   ecs.loadFromFile(File("state.json"))
   ```

## Contributing

Contributions are welcome! Whether it's bug fixes, new features, or documentation improvements, feel free to create a pull request or open an issue.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

## Acknowledgements

Special thanks to the creators of Bevy, Godot, and KaPlay for their inspiration and contributions to the game development community.

---

Aabsalon © 2025 by Brian Flores is licensed under CC BY-SA 4.0 
