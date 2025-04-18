package no.uio.ifi.in2000.met2025.domain.helperclasses

// Simple LinkedList class
// Does not resize
// Not intended for fast access at arbitrary indices
// Apparently does not exist natively in Kotlin?
class SimpleLinkedList<T>(
    initialValue: T? = null
) {
    class Node<T>(val value: T) {
        var next: Node<T>? = null
    }

    private var head: Node<T>? = if (initialValue != null) Node(initialValue) else null
    private var tail: Node<T>? = head

    fun add(value: T) {
        val newNode = Node(value)
        if (tail == null) {
            head = newNode
            tail = newNode
        } else {
            tail?.next = newNode
            tail = newNode
        }
    }

    operator fun plusAssign(value: T) {
        add(value)
    }

    fun add(list: SimpleLinkedList<T>) {
        tail?.next = list.head
        tail = list.tail
    }

    operator fun plusAssign(list: SimpleLinkedList<T>) {
        add(list)
    }

    fun head(): T? = head?.value

    fun tail(): T? = tail?.value

    fun toList(): List<T> {
        val result = mutableListOf<T>()
        var current = head
        while (current != null) {
            result.add(current.value)
            current = current.next
        }
        return result.toList()
    }
}