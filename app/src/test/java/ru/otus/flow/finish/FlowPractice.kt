package ru.otus.flow.finish

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.channels.Channel.Factory.RENDEZVOUS
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import ru.otus.flow.Animal
import ru.otus.flow.Animal.Bird
import ru.otus.flow.Animal.Cat
import ru.otus.flow.Animal.Dog
import ru.otus.flow.Animal.Fish
import ru.otus.flow.finish.FlowPractice.FakeAnimalApi.Callback
import kotlin.system.measureTimeMillis

class FlowPractice {

    @Before
    fun before() {
        println("\n--------------------")
        println("--- Test started ---\n")
    }

    @After
    fun after() {
        println("\n--- Test ended -----")
        println("--------------------")
    }

    @Test
    fun channel_basic() {
        runBlocking {
            val channel = Channel<Animal>()
            launch {
                channel.send(Cat)
            }

            launch {
                val animal = channel.receive()
                println("$animal received") // Cat received
            }
        }
    }

    @Test
    fun channel_consumeEach() {
        runBlocking {
            val channel = Channel<Animal>()
            launch {
                channel.send(Cat)
                channel.send(Dog)
                channel.send(Fish)
                channel.close()
            }

            launch {
                channel.consumeEach { animal ->
                    println("$animal received")
                }
            }
        }
    }

    @Test
    fun channel_produce() {
        runBlocking {
            val channel = produce {
                send(Cat)
                send(Dog)
                send(Fish)
            }

            launch {
                channel.consumeEach { animal ->
                    println("$animal received")
                }
            }
        }
    }

    @Test
    fun channel_produce_with_capacity() {
        runBlocking {
            val channel = produce(capacity = 2) {
                send(Cat)
                println("Cat has been sent")
                send(Dog)
                println("Dog has been sent")
                send(Fish)
                println("Fish has been sent")
            }

            launch {
                delay(100)
                println("${channel.receive()} received")
                delay(100)
                println("${channel.receive()} received")
                delay(100)
                println("${channel.receive()} received")
            }
        }
    }

    @Test
    fun channel_produce_conflate() {
        runBlocking {
            val channel = produce(capacity = CONFLATED) {
                send(Cat)
                println("Cat has been sent")
                send(Dog)
                println("Dog has been sent")
                send(Fish)
                println("Fish has been sent")
            }

            launch {
                channel.consumeEach { animal ->
                    println("$animal received")
                }
            }
        }
    }

    @Test
    fun channel_produce_rendezvous() {
        runBlocking {
            var channel : ReceiveChannel<Animal> = Channel()
            launch {
                channel = produce(capacity = RENDEZVOUS) {
                    send(Cat)
                    println("Cat has been sent")
                    send(Dog)
                    println("Dog has been sent")
                    send(Fish)
                    println("Fish has been sent")
                }
            }

            delay(100)

            launch {
                channel.consumeEach { animal ->
                    println("$animal received")
                }
            }
        }
    }

    @Test
    fun channel_produce_unlimited() {
        runBlocking {
            var channel : ReceiveChannel<Animal> = Channel()
            launch {
                channel = produce(capacity = UNLIMITED) {
                    send(Cat)
                    println("Cat has been sent")
                    send(Dog)
                    println("Dog has been sent")
                    send(Fish)
                    println("Fish has been sent")
                }
            }

            delay(100)

            launch {
                channel.consumeEach { animal ->
                    println("$animal received")
                }
            }
        }
    }

    @Test
    fun flow_builders() {
        runBlocking {
            println("1)")
            flow {
                emit(Cat)
                emit(Dog)
            }
                .collect { animal -> println("$animal received") }

            println("2)")
            flowOf(Cat, Dog).collect { animal -> println("$animal received") }

            println("3)")
            Animal.values().asFlow().collect { animal -> println("$animal received") }
        }
    }

    @Test
    fun flow_basic_intermediate_operators() {
        runBlocking {
            Animal.values().asFlow()
                .filterNot { animal -> animal == Fish }
                .map { animal -> "Robot of $animal"}
                .flatMapConcat { robot ->
                    flow {
                        emit("head of $robot")
                        emit("body of $robot")
                        emit("hands of $robot")
                        emit("legs of $robot")
                    }
                }
                .onStart {
                    println("Starting work")
                }
                .collect { part -> println("$part received") }
        }
    }

    private fun flowOfNumbers(n: Int, delay: Long = 0L, withLog: Boolean = false) = flow {
        for (i in 1..n) {
            delay(delay)
            if (withLog) println("  Before emit $i")
            emit(i)
            if (withLog) println("  After emit $i")
        }
    }

    private fun flowOfAnimals(delay: Long = 0L, withLog: Boolean = false) = flow {
        delay(delay)
        if (withLog) println("  Before emit Cat")
        emit(Cat)
        if (withLog) println("  After emit Cat")
        delay(delay)
        if (withLog) println("  Before emit Dog")
        emit(Dog)
        if (withLog) println("  After emit Dog")
        delay(delay)
        if (withLog) println("  Before emit Fish")
        emit(Fish)
        if (withLog) println("  After emit Fish")
        delay(delay)
        if (withLog) println("  Before emit Bird")
        emit(Bird)
        if (withLog) println("  After emit Bird")
    }

    @Test
    fun flow_flatMap() {
        runBlocking {
            val flow1 = flowOfNumbers(n = 3, delay = 30L)
            val flow2 = flowOfAnimals(delay = 20L)

            println("\nflatMapConcat")
            flow1
                .flatMapConcat { index ->
                    flow2.map { animal -> "$index-$animal" }
                }
                .collect { println("    -> $it received") }

            println("\nflatMapMerge")
            flow1
                .flatMapMerge { index ->
                    flow2.map { animal -> "$index-$animal" }
                }
                .collect { println("$it received") }

            println("\nflatMapLatest")
            flow1
                .flatMapLatest { index ->
                    flow2.map { animal -> "$index-$animal" }
                }
                .collect { println("$it received") }
        }
    }

    @Test
    fun flow_customOperator() {
        runBlocking {
            val flow = flow {
                emit(Cat)
                emit(Dog)
                delay(310)
                emit(Dog)
                emit(Fish)
                emit(Fish)
                emit(Fish)
                emit(Bird)
            }

            flow.distinctUntilTime(300)
                .collect { animal ->
                    println("$animal collected")
                }
        }
    }

    fun <T> Flow<T>.distinctUntilTime(time: Long): Flow<T> {
        return flow {
            var lastTime = System.currentTimeMillis()
            var lastValue: T? = null
            collect { value ->
                if (value != lastValue || (System.currentTimeMillis() - lastTime) >= time) {
                    emit(value)
                }
                lastValue = value
                lastTime = System.currentTimeMillis()
            }
        }
    }

    @Test
    fun flow_combining() {
        runBlocking {
            val flow1 = flowOfNumbers(n = 3, delay = 10L)
            val flow2 = flowOfAnimals(delay = 50L)

            println("Zip")
            flow1.zip(flow2) { index, animal ->
                "$index-$animal"
            }
                .collect { zipped ->
                    println("$zipped collected")
                }

            println("combine")
            combine(
                flow1,
                flow2
            ) { index, animal ->
                "$index-$animal"
            }
                .collect { combined ->
                    println("$combined collected")
                }

            println("merge")
            merge(
                flow1,
                flow2
            )
                .collect { merged ->
                    println("$merged collected")
                }
        }
    }

    @Test
    fun flow_with_buffer() {
        runBlocking {
            val flow = flowOfAnimals(delay = 90L, withLog = true)

            val time = measureTimeMillis {
                flow
                    .buffer()
                    .collect { animal ->
                        delay(100L)
                        println("$animal collected")
                    }
            }
            println("Took $time millis")
        }
    }

    @Test
    fun flow_channel_flow() {
        runBlocking {
            val flow = channelFlow {
                Animal.values().asFlow()
                    .collect { animal ->
                        if (animal == Fish) {
                            channel.close()
                        } else {
                            if (!isClosedForSend) send(animal)
                        }
                    }
                awaitClose()
            }

            val time = measureTimeMillis {
                flow.collect { animal ->
                    delay(80)
                    println("$animal collected")
                }
            }
            println("Took $time millis")
        }
    }

    class FakeAnimalApi {
        interface Callback {
            fun onDataReady(animals: List<Animal>)
        }
        suspend fun getAnimals(callback: Callback) {
            delay(300)
            callback.onDataReady(Animal.values().toMutableList().apply { remove(Cat) })
            delay(500)
            callback.onDataReady(Animal.values().toList())
        }
    }

    @Test
    fun flow_callback_flow() {
        runBlocking {
            val api = FakeAnimalApi()
            val flow = callbackFlow {
                val callback = object : Callback {
                    override fun onDataReady(animals: List<Animal>) {
                        println("onDataReady $animals")
                        animals.forEach { animal ->
                            trySend(animal)
                        }
                        if (animals.contains(Cat)) {
                            channel.close()
                        }
                    }
                }

                api.getAnimals(callback)

                awaitClose {
                    println("Releasing resources")
                }
            }

            flow.collect { animal ->
                println("$animal collected")
            }
        }
    }

    @Test
    fun sharedFlow() {
        runBlocking {
            val flow = MutableSharedFlow<Animal>(replay = 1, extraBufferCapacity = 2)

            val job1 = launch {
                flow.tryEmit(Cat)
                delay(100)
                flow.tryEmit(Dog)
                delay(100)
                flow.tryEmit(Fish)
                delay(100)
            }

            delay(50)

            val job2 = launch {
                flow.collect { animal ->
                    println("$animal collected")
                }
            }

            delay(1000)
            job1.cancel()
            job2.cancel()
        }
    }

    @Test
    fun stateFlow() {
        runBlocking {
            val flow = MutableStateFlow(Cat)

            val job1 = launch {
                delay(100)
                flow.tryEmit(Dog)
                delay(100)
                flow.tryEmit(Fish)
                delay(100)
            }

            delay(120)
            println("${flow.value} checked")

            val job2 = launch {
                flow.collect { animal ->
                    println("$animal collected")
                }
            }

            delay(1000)
            println("${flow.value} checked")

            delay(1000)
            job1.cancel()
            job2.cancel()
        }
    }
}