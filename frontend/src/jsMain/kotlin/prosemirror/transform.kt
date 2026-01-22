@file:JsModule("prosemirror-transform")
@file:JsNonModule

package prosemirror.transform

import prosemirror.model.Attrs
import prosemirror.model.ContentMatch
import prosemirror.model.Fragment
import prosemirror.model.Mark
import prosemirror.model.MarkType
import prosemirror.model.Node
import prosemirror.model.NodeRange
import prosemirror.model.NodeType
import prosemirror.model.Schema
import prosemirror.model.Slice
import kotlin.js.ReadonlyArray

/**
There are several things that positions can be mapped through.
Such objects conform to this interface.
*/
external interface Mappable {
    /**
    Map a position through this object. When given, `assoc` (should
    be -1 or 1, defaults to 1) determines with which side the
    position is associated, which determines in which direction to
    move when a chunk of content is inserted at the mapped position.
    */
    var map: (pos: Double, assoc: Double?) -> Double
    /**
    Map a position, and return an object containing additional
    information about the mapping. The result's `deleted` field tells
    you whether the position was deleted (completely enclosed in a
    replaced range) during the mapping. When content on only one side
    is deleted, the position itself is only considered deleted when
    `assoc` points in the direction of the deleted content.
    */
    var mapResult: (pos: Double, assoc: Double?) -> MapResult
}
/**
An object representing a mapped position with extra
information.
*/
external open class MapResult {
    /**
    The mapped version of the position.
    */
    val pos: Double
    /**
    Tells you whether the position was deleted, that is, whether the
    step removed the token on the side queried (via the `assoc`)
    argument from the document.
    */
    val deleted: Boolean
    /**
    Tells you whether the token before the mapped position was deleted.
    */
    val deletedBefore: Boolean
    /**
    True when the token after the mapped position was deleted.
    */
    val deletedAfter: Boolean
    /**
    Tells whether any of the steps mapped through deletes across the
    position (including both the token before and after the
    position).
    */
    val deletedAcross: Boolean
}
/**
A map describing the deletions and insertions made by a step, which
can be used to find the correspondence between positions in the
pre-step version of a document and the same position in the
post-step version.
*/
external open class StepMap : Mappable {
    /**
    Create a position map. The modifications to the document are
    represented as an array of numbers, in which each group of three
    represents a modified chunk as `[start, oldSize, newSize]`.
    */
    constructor(
    /**
    @internal
    */
    ranges: ReadonlyArray<Double>, 
    /**
    @internal
    */
    inverted: Boolean? = definedExternally)
    override var mapResult: (pos: Double, assoc: Double?) -> MapResult
    override var map: (pos: Double, assoc: Double?) -> Double
    /**
    Calls the given function on each of the changed ranges included in
    this map.
    */
    fun forEach(f: (oldStart: Double, oldEnd: Double, newStart: Double, newEnd: Double) -> Unit): Unit
    /**
    Create an inverted version of this map. The result can be used to
    map positions in the post-step document to the pre-step document.
    */
    fun invert(): StepMap

    companion object {
        /**
        Create a map that moves all positions by offset `n` (which may be
        negative). This can be useful when applying steps meant for a
        sub-document to a larger document, or vice-versa.
        */
        fun offset(n: Double): StepMap
        /**
        A StepMap that contains no changed ranges.
        */
        val empty: StepMap
    }
}
/**
A mapping represents a pipeline of zero or more [step
maps](https://prosemirror.net/docs/ref/#transform.StepMap). It has special provisions for losslessly
handling mapping positions through a series of steps in which some
steps are inverted versions of earlier steps. (This comes up when
‘[rebasing](https://prosemirror.net/docs/guide/#transform.rebasing)’ steps for
collaboration or history management.)
*/
external open class Mapping : Mappable {
    /**
    The starting position in the `maps` array, used when `map` or
    `mapResult` is called.
    */
    var from: Double
    /**
    The end position in the `maps` array.
    */
    var to: Double
    /**
    Create a new mapping with the given position maps.
    */
    constructor(maps: ReadonlyArray<StepMap>? = definedExternally, 
    /**
    @internal
    */
    mirror: ReadonlyArray<Double>? = definedExternally, 
    /**
    The starting position in the `maps` array, used when `map` or
    `mapResult` is called.
    */
    from: Double? = definedExternally, 
    /**
    The end position in the `maps` array.
    */
    to: Double? = definedExternally)
    /**
    The step maps in this mapping.
    */
    val maps: ReadonlyArray<StepMap>
    private var _maps: dynamic
    private var ownData: dynamic
    /**
    Create a mapping that maps only through a part of this one.
    */
    fun slice(from: Double? = definedExternally, to: Double? = definedExternally): Mapping
    /**
    Add a step map to the end of this mapping. If `mirrors` is
    given, it should be the index of the step map that is the mirror
    image of this one.
    */
    fun appendMap(map: StepMap, mirrors: Double? = definedExternally): Unit
    /**
    Add all the step maps in a given mapping to this one (preserving
    mirroring information).
    */
    fun appendMapping(mapping: Mapping): Unit
    /**
    Finds the offset of the step map that mirrors the map at the
    given offset, in this mapping (as per the second argument to
    `appendMap`).
    */
    fun getMirror(n: Double): Double?
    /**
    Append the inverse of the given mapping to this one.
    */
    fun appendMappingInverted(mapping: Mapping): Unit
    /**
    Create an inverted version of this mapping.
    */
    fun invert(): Mapping
    /**
    Map a position through this mapping.
    */
    override var map: (pos: Double, assoc: Double?) -> Double
    /**
    Map a position through this mapping, returning a mapping
    result.
    */
    override var mapResult: (pos: Double, assoc: Double?) -> MapResult
}

/**
A step object represents an atomic change. It generally applies
only to the document it was created for, since the positions
stored in it will only make sense for that document.

New steps are defined by creating classes that extend `Step`,
overriding the `apply`, `invert`, `map`, `getMap` and `fromJSON`
methods, and registering your class with a unique
JSON-serialization identifier using
[`Step.jsonID`](https://prosemirror.net/docs/ref/#transform.Step^jsonID).
*/
external abstract class Step {
    /**
    Applies this step to the given document, returning a result
    object that either indicates failure, if the step can not be
    applied to this document, or indicates success by containing a
    transformed document.
    */
    abstract fun apply(doc: Node): StepResult
    /**
    Get the step map that represents the changes made by this step,
    and which can be used to transform between positions in the old
    and the new document.
    */
    open fun getMap(): StepMap
    /**
    Create an inverted version of this step. Needs the document as it
    was before the step as argument.
    */
    abstract fun invert(doc: Node): Step
    /**
    Map this step through a mappable thing, returning either a
    version of that step with its positions adjusted, or `null` if
    the step was entirely deleted by the mapping.
    */
    abstract fun map(mapping: Mappable): Step?
    /**
    Try to merge this step with another one, to be applied directly
    after it. Returns the merged step when possible, null if the
    steps can't be merged.
    */
    open fun merge(other: Step): Step?
    /**
    Create a JSON-serializeable representation of this step. When
    defining this for a custom subclass, make sure the result object
    includes the step type's [JSON id](https://prosemirror.net/docs/ref/#transform.Step^jsonID) under
    the `stepType` property.
    */
    abstract fun toJSON(): Any?

    companion object {
        /**
        Deserialize a step from its JSON representation. Will call
        through to the step class' own implementation of this method.
        */
        fun fromJSON(schema: Schema<dynamic, dynamic>, json: Any?): Step
        /**
        To be able to serialize steps to JSON, each step needs a string
        ID to attach to its JSON representation. Use this method to
        register an ID for your step classes. Try to pick something
        that's unlikely to clash with steps from other modules.
        */
        fun jsonID(id: String, stepClass: StepJsonID): StepJsonID
    }
}
/**
The result of [applying](https://prosemirror.net/docs/ref/#transform.Step.apply) a step. Contains either a
new document or a failure value.
*/
external open class StepResult {
    /**
    The transformed document, if successful.
    */
    val doc: Node?
    /**
    The failure message, if unsuccessful.
    */
    val failed: String?

    companion object {
        /**
        Create a successful step result.
        */
        fun ok(doc: Node): StepResult
        /**
        Create a failed step result.
        */
        fun fail(message: String): StepResult
        /**
        Call [`Node.replace`](https://prosemirror.net/docs/ref/#model.Node.replace) with the given
        arguments. Create a successful result if it succeeds, and a
        failed one if it throws a `ReplaceError`.
        */
        fun fromReplace(doc: Node, from: Double, to: Double, slice: Slice): StepResult
    }
}

/**
Abstraction to build up and track an array of
[steps](https://prosemirror.net/docs/ref/#transform.Step) representing a document transformation.

Most transforming methods return the `Transform` object itself, so
that they can be chained.
*/
external open class Transform {
    /**
    The current document (the result of applying the steps in the
    transform).
    */
    var doc: Node
    /**
    The steps in this transform.
    */
    val steps: ReadonlyArray<Step>
    /**
    The documents before each of the steps.
    */
    val docs: ReadonlyArray<Node>
    /**
    A mapping with the maps for each of the steps in this transform.
    */
    val mapping: Mapping
    /**
    Create a transform that starts with the given document.
    */
    constructor(
    /**
    The current document (the result of applying the steps in the
    transform).
    */
    doc: Node)
    /**
    The starting document.
    */
    val before: Node
    /**
    Apply a new step in this transform, saving the result. Throws an
    error when the step fails.
    */
    fun step(step: Step): Transform
    /**
    Try to apply a step in this transformation, ignoring it if it
    fails. Returns the step result.
    */
    fun maybeStep(step: Step): StepResult
    /**
    True when the document has been changed (when there are any
    steps).
    */
    val docChanged: Boolean
    /**
    Return a single range, in post-transform document positions,
    that covers all content changed by this transform. Returns null
    if no replacements are made. Note that this will ignore changes
    that add/remove marks without replacing the underlying content.
    */
    fun changedRange(): ChangedRange?
    /**
    Replace the part of the document between `from` and `to` with the
    given `slice`.
    */
    fun replace(from: Double, to: Double? = definedExternally, slice: Slice? = definedExternally): Transform
    /**
    Replace the given range with the given content, which may be a
    fragment, node, or array of nodes.
    */
    fun replaceWith(from: Double, to: Double, content: dynamic): Transform
    /**
    Delete the content between the given positions.
    */
    fun delete(from: Double, to: Double): Transform
    /**
    Insert the given content at the given position.
    */
    fun insert(pos: Double, content: dynamic): Transform
    /**
    Replace a range of the document with a given slice, using
    `from`, `to`, and the slice's
    [`openStart`](https://prosemirror.net/docs/ref/#model.Slice.openStart) property as hints, rather
    than fixed start and end points. This method may grow the
    replaced area or close open nodes in the slice in order to get a
    fit that is more in line with WYSIWYG expectations, by dropping
    fully covered parent nodes of the replaced region when they are
    marked [non-defining as
    context](https://prosemirror.net/docs/ref/#model.NodeSpec.definingAsContext), or including an
    open parent node from the slice that _is_ marked as [defining
    its content](https://prosemirror.net/docs/ref/#model.NodeSpec.definingForContent).
    
    This is the method, for example, to handle paste. The similar
    [`replace`](https://prosemirror.net/docs/ref/#transform.Transform.replace) method is a more
    primitive tool which will _not_ move the start and end of its given
    range, and is useful in situations where you need more precise
    control over what happens.
    */
    fun replaceRange(from: Double, to: Double, slice: Slice): Transform
    /**
    Replace the given range with a node, but use `from` and `to` as
    hints, rather than precise positions. When from and to are the same
    and are at the start or end of a parent node in which the given
    node doesn't fit, this method may _move_ them out towards a parent
    that does allow the given node to be placed. When the given range
    completely covers a parent node, this method may completely replace
    that parent node.
    */
    fun replaceRangeWith(from: Double, to: Double, node: Node): Transform
    /**
    Delete the given range, expanding it to cover fully covered
    parent nodes until a valid replace is found.
    */
    fun deleteRange(from: Double, to: Double): Transform
    /**
    Split the content in the given range off from its parent, if there
    is sibling content before or after it, and move it up the tree to
    the depth specified by `target`. You'll probably want to use
    [`liftTarget`](https://prosemirror.net/docs/ref/#transform.liftTarget) to compute `target`, to make
    sure the lift is valid.
    */
    fun lift(range: NodeRange, target: Double): Transform
    /**
    Join the blocks around the given position. If depth is 2, their
    last and first siblings are also joined, and so on.
    */
    fun join(pos: Double, depth: Double? = definedExternally): Transform
    /**
    Wrap the given [range](https://prosemirror.net/docs/ref/#model.NodeRange) in the given set of wrappers.
    The wrappers are assumed to be valid in this position, and should
    probably be computed with [`findWrapping`](https://prosemirror.net/docs/ref/#transform.findWrapping).
    */
    fun wrap(range: NodeRange, wrappers: ReadonlyArray<WrapRange>): Transform
    /**
    Set the type of all textblocks (partly) between `from` and `to` to
    the given node type with the given attributes.
    */
    fun setBlockType(from: Double, to: Double? = definedExternally, type: NodeType, attrs: dynamic = definedExternally): Transform
    /**
    Change the type, attributes, and/or marks of the node at `pos`.
    When `type` isn't given, the existing node type is preserved,
    */
    fun setNodeMarkup(pos: Double, type: NodeType? = definedExternally, attrs: Attrs? = definedExternally, marks: ReadonlyArray<Mark>? = definedExternally): Transform
    /**
    Set a single attribute on a given node to a new value.
    The `pos` addresses the document content. Use `setDocAttribute`
    to set attributes on the document itself.
    */
    fun setNodeAttribute(pos: Double, attr: String, value: Any?): Transform
    /**
    Set a single attribute on the document to a new value.
    */
    fun setDocAttribute(attr: String, value: Any?): Transform
    /**
    Add a mark to the node at position `pos`.
    */
    fun addNodeMark(pos: Double, mark: Mark): Transform
    /**
    Remove a mark (or all marks of the given type) from the node at
    position `pos`.
    */
    fun removeNodeMark(pos: Double, mark: dynamic): Transform
    /**
    Split the node at the given position, and optionally, if `depth` is
    greater than one, any number of nodes above that. By default, the
    parts split off will inherit the node type of the original node.
    This can be changed by passing an array of types and attributes to
    use after the split (with the outermost nodes coming first).
    */
    fun split(pos: Double, depth: Double? = definedExternally, typesAfter: ReadonlyArray<SplitType?>? = definedExternally): Transform
    /**
    Add the given mark to the inline content between `from` and `to`.
    */
    fun addMark(from: Double, to: Double, mark: Mark): Transform
    /**
    Remove marks from inline nodes between `from` and `to`. When
    `mark` is a single mark, remove precisely that mark. When it is
    a mark type, remove all marks of that type. When it is null,
    remove all marks of any type.
    */
    fun removeMark(from: Double, to: Double, mark: dynamic = definedExternally): Transform
    /**
    Removes all marks and nodes from the content of the node at
    `pos` that don't match the given new parent node type. Accepts
    an optional starting [content match](https://prosemirror.net/docs/ref/#model.ContentMatch) as
    third argument.
    */
    fun clearIncompatible(pos: Double, parentType: NodeType, match: ContentMatch? = definedExternally): Transform
}

/**
Try to find a target depth to which the content in the given range
can be lifted. Will not go across
[isolating](https://prosemirror.net/docs/ref/#model.NodeSpec.isolating) parent nodes.
*/
external fun liftTarget(range: NodeRange): Double?
/**
Try to find a valid way to wrap the content in the given range in a
node of the given type. May introduce extra nodes around and inside
the wrapper node, if necessary. Returns null if no valid wrapping
could be found. When `innerRange` is given, that range's content is
used as the content to fit into the wrapping, instead of the
content of `range`.
*/
external fun findWrapping(range: NodeRange, nodeType: NodeType, attrs: Attrs? = definedExternally, innerRange: NodeRange? = definedExternally): ReadonlyArray<WrapResult>?
/**
Check whether splitting at the given position is allowed.
*/
external fun canSplit(doc: Node, pos: Double, depth: Double? = definedExternally, typesAfter: ReadonlyArray<SplitType?>? = definedExternally): Boolean
/**
Test whether the blocks before and after a given position can be
joined.
*/
external fun canJoin(doc: Node, pos: Double): Boolean
/**
Find an ancestor of the given position that can be joined to the
block before (or after if `dir` is positive). Returns the joinable
point, if any.
*/
external fun joinPoint(doc: Node, pos: Double, dir: Double? = definedExternally): Double?
/**
Try to find a point where a node of the given type can be inserted
near `pos`, by searching up the node hierarchy when `pos` itself
isn't a valid place but is at the start or end of a node. Return
null if no position was found.
*/
external fun insertPoint(doc: Node, pos: Double, nodeType: NodeType): Double?
/**
Finds a position at or around the given position where the given
slice can be inserted. Will look at parent nodes' nearest boundary
and try there, even if the original position wasn't directly at the
start or end of that node. Returns null when no position was found.
*/
external fun dropPoint(doc: Node, pos: Double, slice: Slice): Double?

/**
Add a mark to all inline content between two positions.
*/
external open class AddMarkStep : Step {
    /**
    The start of the marked range.
    */
    val from: Double
    /**
    The end of the marked range.
    */
    val to: Double
    /**
    The mark to add.
    */
    val mark: Mark
    /**
    Create a mark step.
    */
    constructor(
    /**
    The start of the marked range.
    */
    from: Double, 
    /**
    The end of the marked range.
    */
    to: Double, 
    /**
    The mark to add.
    */
    mark: Mark)
    override fun apply(doc: Node): StepResult
    fun invert(): Step
    override fun map(mapping: Mappable): Step?
    override fun merge(other: Step): Step?
    override fun toJSON(): Any?
}
/**
Remove a mark from all inline content between two positions.
*/
external open class RemoveMarkStep : Step {
    /**
    The start of the unmarked range.
    */
    val from: Double
    /**
    The end of the unmarked range.
    */
    val to: Double
    /**
    The mark to remove.
    */
    val mark: Mark
    /**
    Create a mark-removing step.
    */
    constructor(
    /**
    The start of the unmarked range.
    */
    from: Double, 
    /**
    The end of the unmarked range.
    */
    to: Double, 
    /**
    The mark to remove.
    */
    mark: Mark)
    override fun apply(doc: Node): StepResult
    fun invert(): Step
    override fun map(mapping: Mappable): Step?
    override fun merge(other: Step): Step?
    override fun toJSON(): Any?
}
/**
Add a mark to a specific node.
*/
external open class AddNodeMarkStep : Step {
    /**
    The position of the target node.
    */
    val pos: Double
    /**
    The mark to add.
    */
    val mark: Mark
    /**
    Create a node mark step.
    */
    constructor(
    /**
    The position of the target node.
    */
    pos: Double, 
    /**
    The mark to add.
    */
    mark: Mark)
    override fun apply(doc: Node): StepResult
    override fun invert(doc: Node): Step
    override fun map(mapping: Mappable): Step?
    override fun toJSON(): Any?
}
/**
Remove a mark from a specific node.
*/
external open class RemoveNodeMarkStep : Step {
    /**
    The position of the target node.
    */
    val pos: Double
    /**
    The mark to remove.
    */
    val mark: Mark
    /**
    Create a mark-removing step.
    */
    constructor(
    /**
    The position of the target node.
    */
    pos: Double, 
    /**
    The mark to remove.
    */
    mark: Mark)
    override fun apply(doc: Node): StepResult
    override fun invert(doc: Node): Step
    override fun map(mapping: Mappable): Step?
    override fun toJSON(): Any?
}

/**
Replace a part of the document with a slice of new content.
*/
external open class ReplaceStep : Step {
    /**
    The start position of the replaced range.
    */
    val from: Double
    /**
    The end position of the replaced range.
    */
    val to: Double
    /**
    The slice to insert.
    */
    val slice: Slice
    /**
    The given `slice` should fit the 'gap' between `from` and
    `to`—the depths must line up, and the surrounding nodes must be
    able to be joined with the open sides of the slice. When
    `structure` is true, the step will fail if the content between
    from and to is not just a sequence of closing and then opening
    tokens (this is to guard against rebased replace steps
    overwriting something they weren't supposed to).
    */
    constructor(
    /**
    The start position of the replaced range.
    */
    from: Double, 
    /**
    The end position of the replaced range.
    */
    to: Double, 
    /**
    The slice to insert.
    */
    slice: Slice, 
    /**
    @internal
    */
    structure: Boolean? = definedExternally)
    override fun apply(doc: Node): StepResult
    override fun getMap(): StepMap
    override fun invert(doc: Node): ReplaceStep
    override fun map(mapping: Mappable): ReplaceStep?
    override fun merge(other: Step): ReplaceStep?
    override fun toJSON(): Any?
}
/**
Replace a part of the document with a slice of content, but
preserve a range of the replaced content by moving it into the
slice.
*/
external open class ReplaceAroundStep : Step {
    /**
    The start position of the replaced range.
    */
    val from: Double
    /**
    The end position of the replaced range.
    */
    val to: Double
    /**
    The start of preserved range.
    */
    val gapFrom: Double
    /**
    The end of preserved range.
    */
    val gapTo: Double
    /**
    The slice to insert.
    */
    val slice: Slice
    /**
    The position in the slice where the preserved range should be
    inserted.
    */
    val insert: Double
    /**
    Create a replace-around step with the given range and gap.
    `insert` should be the point in the slice into which the content
    of the gap should be moved. `structure` has the same meaning as
    it has in the [`ReplaceStep`](https://prosemirror.net/docs/ref/#transform.ReplaceStep) class.
    */
    constructor(
    /**
    The start position of the replaced range.
    */
    from: Double, 
    /**
    The end position of the replaced range.
    */
    to: Double, 
    /**
    The start of preserved range.
    */
    gapFrom: Double, 
    /**
    The end of preserved range.
    */
    gapTo: Double, 
    /**
    The slice to insert.
    */
    slice: Slice, 
    /**
    The position in the slice where the preserved range should be
    inserted.
    */
    insert: Double, 
    /**
    @internal
    */
    structure: Boolean? = definedExternally)
    override fun apply(doc: Node): StepResult
    override fun getMap(): StepMap
    override fun invert(doc: Node): ReplaceAroundStep
    override fun map(mapping: Mappable): ReplaceAroundStep?
    override fun toJSON(): Any?
}

/**
Update an attribute in a specific node.
*/
external open class AttrStep : Step {
    /**
    The position of the target node.
    */
    val pos: Double
    /**
    The attribute to set.
    */
    val attr: String
    val value: Any?
    /**
    Construct an attribute step.
    */
    constructor(
    /**
    The position of the target node.
    */
    pos: Double, 
    /**
    The attribute to set.
    */
    attr: String, value: Any?)
    override fun apply(doc: Node): StepResult
    override fun getMap(): StepMap
    override fun invert(doc: Node): AttrStep
    override fun map(mapping: Mappable): AttrStep?
    override fun toJSON(): Any?

    companion object {
        fun fromJSON(schema: Schema<dynamic, dynamic>, json: Any?): AttrStep
    }
}
/**
Update an attribute in the doc node.
*/
external open class DocAttrStep : Step {
    /**
    The attribute to set.
    */
    val attr: String
    val value: Any?
    /**
    Construct an attribute step.
    */
    constructor(
    /**
    The attribute to set.
    */
    attr: String, value: Any?)
    override fun apply(doc: Node): StepResult
    override fun getMap(): StepMap
    override fun invert(doc: Node): DocAttrStep
    override fun map(mapping: Mappable): DocAttrStep
    override fun toJSON(): Any?

    companion object {
        fun fromJSON(schema: Schema<dynamic, dynamic>, json: Any?): DocAttrStep
    }
}

/**
‘Fit’ a slice into a given position in the document, producing a
[step](https://prosemirror.net/docs/ref/#transform.Step) that inserts it. Will return null if
there's no meaningful way to insert the slice here, or inserting it
would be a no-op (an empty slice over an empty range).
*/
external fun replaceStep(doc: Node, from: Double, to: Double? = definedExternally, slice: Slice? = definedExternally): Step?

external interface ChangedRange {
    var from: Double
    var to: Double
}

external interface WrapRange {
    var type: NodeType
    var attrs: Attrs?
}

external interface WrapResult {
    var type: NodeType
    var attrs: Attrs?
}

external interface SplitType {
    var type: NodeType
    var attrs: Attrs?
}

external interface StepJsonID {
    var fromJSON: (schema: Schema<dynamic, dynamic>, json: Any?) -> Step
}
