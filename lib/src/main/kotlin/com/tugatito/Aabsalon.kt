package com.tugatito

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.util.concurrent.Executors
import kotlin.reflect.KClass

// Aliases for better readability
typealias Actor = Int
typealias Attributes = MutableMap<KClass<out Attribute>, Attribute>
typealias Behavior = (manager: Aabsalon) -> Unit

// Base interface for attributes. Marked as sealed to support serialization.
@Serializable
sealed interface Attribute

// Enum representing different lifecycle phases of the ECS
enum class Phase {
  Init,        // Initialization phase
  PreUpdate,   // Phase before update logic
  Update,      // Main update logic
  PostUpdate,  // Phase after update logic
  Draw,        // Drawing/rendering phase
  End          // Cleanup phase
}

// Serializable data class to save the ECS state
@Serializable
data class AabsalonState(
  val actors: Set<Actor>,                        // Set of actor IDs
  val attributes: Map<Actor, Attributes>         // Mapping of actor IDs to their attributes
)

// Core ECS manager
class Aabsalon(private val enableCache: Boolean = true) {
  private val executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
  private var actorsCount: Int = 0                         // Tracks the number of created actors
  private val actors = mutableSetOf<Actor>()               // Set of active actors
  private val attributes = mutableMapOf<Actor, Attributes>() // Maps actors to their attributes
  private val behaviors = mutableMapOf<Phase, MutableList<Behavior>>().apply {
    Phase.values().forEach { put(it, mutableListOf()) }  // Initialize behavior lists for each phase
  }
  private val actorsCache = if (enableCache) mutableMapOf<Set<KClass<out Attribute>>, List<Actor>>() else null // Cache for quick queries

  // Clears the attribute cache
  private fun invalidateCache() {
    actorsCache?.clear()
  }

  // Creates a new actor with the given attributes
  fun createActor(newAttributes: Array<Attribute>): Aabsalon {
    val attrs = newAttributes.associateBy { it::class }.toMutableMap()
    attributes[actorsCount] = attrs
    actors.add(actorsCount)
    actorsCount++
    return this
  }

  // Checks if an actor exists
  fun ensureActorExists(actor: Actor): Boolean {
    return actors.contains(actor)
  }

  // Retrieves actors with specific attributes
  fun getActorsWith(vararg attributeClasses: KClass<out Attribute>): List<Actor> {
    val key = attributeClasses.toSet()
    if (enableCache) {
      return actorsCache!!.getOrPut(key) {
        actors.filter { actor ->
          key.all { attributes[actor]?.containsKey(it) == true }
        }
      }
    }
    // Without cache
    return actors.filter { actor ->
      key.all { attributes[actor]?.containsKey(it) == true }
    }
  }

  // Removes an actor and its attributes
  fun destroyActor(actor: Actor): Aabsalon {
    actors.remove(actor)
    attributes.remove(actor)
    invalidateCache()
    return this
  }

  // Adds a new attribute to an actor
  fun addAttribute(actor: Actor, attribute: Attribute): Aabsalon {
    require(ensureActorExists(actor)) { "Actor $actor does not exist." }
    attributes.computeIfAbsent(actor) { mutableMapOf() }[attribute::class] = attribute
    invalidateCache()
    return this
  }

  // Retrieves a specific attribute from an actor
  fun <T : Attribute> getAttribute(actor: Actor, attribute: KClass<T>): T? {
    require(ensureActorExists(actor)) { "Actor $actor does not exist." }
    return attributes[actor]?.get(attribute) as? T
  }

  // Removes a specific attribute from an actor
  fun <T : Attribute> removeAttribute(actor: Actor, attribute: KClass<T>): Aabsalon {
    invalidateCache()
    attributes[actor]?.remove(attribute)
    return this
  }

  // Adds a behavior to a specific phase, with optional parallel execution
  fun addBehavior(phase: Phase, behavior: Behavior, parallel: Boolean = false): Aabsalon {
    if (parallel) {
      val parallelBehavior: Behavior = { manager ->
        executor.submit { behavior(manager) }
      }
      behaviors[phase]?.addLast(parallelBehavior)
    } else {
      behaviors[phase]?.addLast(behavior)
    }
    return this
  }

  // Serializes the current state to JSON
  fun saveState(): String {
    val state = AabsalonState(
      actors = this.actors,
      attributes = this.attributes.mapValues { (_, attrs) -> attrs.toMutableMap() }
    )
    return Json.encodeToString(AabsalonState.serializer(), state)
  }

  // Deserializes state from JSON and applies it
  fun loadState(json: String): Aabsalon {
    val state = Json.decodeFromString(AabsalonState.serializer(), json)
    this.actors.clear()
    this.actors.addAll(state.actors)
    this.attributes.clear()
    state.attributes.forEach { (actor, attrs) ->
      this.attributes[actor] = attrs.toMutableMap()
    }
    invalidateCache()
    return this
  }

  // Saves state to a file
  fun saveToFile(file: File) {
    file.writeText(saveState())
  }

  // Loads state from a file
  fun loadFromFile(file: File): Aabsalon {
    val json = file.readText()
    return loadState(json)
  }

  // Debugging: prints the current actors and behaviors
  fun debug() {
    println("Actors:")
    actors.forEach { actor ->
      println("  Actor $actor: ${attributes[actor]}")
    }
    println("Behaviors:")
    behaviors.forEach { (phase, behaviorList) ->
      println("  Phase $phase: ${behaviorList.size} behaviors")
    }
  }

  // Executes all behaviors in the Init phase
  fun init() {
    behaviors[Phase.Init]?.forEach { it(this) }
  }

  // Executes all behaviors in the Update phases
  fun update() {
    behaviors[Phase.PreUpdate]?.forEach { it(this) }
    behaviors[Phase.Update]?.forEach { it(this) }
    behaviors[Phase.PostUpdate]?.forEach { it(this) }
  }

  // Executes all behaviors in the Draw phase
  fun draw() {
    behaviors[Phase.Draw]?.forEach { it(this) }
  }

  // Executes all behaviors in the End phase and shuts down the executor
  fun end() {
    behaviors[Phase.End]?.forEach { it(this) }
    executor.shutdown()
  }
}
