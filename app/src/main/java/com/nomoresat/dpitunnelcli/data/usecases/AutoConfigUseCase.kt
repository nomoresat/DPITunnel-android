package com.nomoresat.dpitunnelcli.data.usecases

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.nomoresat.dpitunnelcli.domain.usecases.IAutoConfigUseCase
import java.io.*

class AutoConfigUseCase: IAutoConfigUseCase {

    private var _process: Process? = null
        @Synchronized
        set(value) {
            field?.destroy()
            field?.inputStream?.close()
            field?.errorStream?.close()
            field?.outputStream?.close()
            field = value
        }

    private var _outputStream: OutputStream? = null
    private var _inputFlow = MutableSharedFlow<String>(replay = 0)
    override var inputFlow: SharedFlow<String> = _inputFlow.asSharedFlow()

    override fun run(externalScope: CoroutineScope, cmd: List<String>,
                     exceptionCallBack: (Throwable) -> Unit) {
        externalScope.launch(Dispatchers.IO) {
            runCatching {
                _process = ProcessBuilder(cmd)
                    .redirectErrorStream(true)
                    .start().also { process ->
                        _outputStream = process.outputStream
                        val br = BufferedReader(InputStreamReader(process.inputStream))
                        val stringBuilder = StringBuilder()
                        var c: Int = br.read()
                        while (c != -1) {
                            stringBuilder.append((Char(c)))
                            if (br.ready())
                                c = br.read()
                            else {
                                val str = stringBuilder.toString()
                                _inputFlow.emit(str)
                                stringBuilder.setLength(0)
                                c = br.read()
                            }
                        }
                    }
            }.onFailure(exceptionCallBack)
        }
    }

    override fun input(input: String) {
        try {
            _outputStream?.let {
                OutputStreamWriter(it, Charsets.UTF_8).apply {
                    write(input)
                    flush()
                }
            }
        } catch (e: IOException) {}
    }
}