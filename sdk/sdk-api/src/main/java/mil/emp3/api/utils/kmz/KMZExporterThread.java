package mil.emp3.api.utils.kmz

import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipOutputStream

import mil.emp3.api.interfaces.IEmpExportToStringCallback
import mil.emp3.api.interfaces.IEmpExportToTypeCallBack
import mil.emp3.api.interfaces.IFeature
import mil.emp3.api.interfaces.IMap
import mil.emp3.api.interfaces.IOverlay

/**
 * Created by jenifer.Cochran on 9/28/2017.

 */

class KMZExporterThread : Thread {
    private val outputDirectory: File
    private val callback: IEmpExportToTypeCallBack<File>
    private val map: IMap
    private val extendedData: Boolean
    private val overlay: IOverlay?
    private val feature: IFeature<*>?
    private val exportType: ExportType


    protected enum class ExportType {
        Map,
        Overlay,
        Feature
    }


    constructor(map: IMap,
                extendedData: Boolean,
                callback: IEmpExportToTypeCallBack<File>,
                outputDirectory: String) {
        this.outputDirectory = File(outputDirectory)
        createOutputDirectory(outputDirectory)
        this.callback = callback
        this.map = map
        this.extendedData = extendedData
        this.overlay = null
        this.feature = null
        this.exportType = ExportType.Map
    }


    constructor(map: IMap,
                overlay: IOverlay,
                extendedData: Boolean,
                callback: IEmpExportToTypeCallBack<File>,
                outputDirectory: String) {
        this.outputDirectory = File(outputDirectory)
        createOutputDirectory(outputDirectory)
        this.callback = callback
        this.map = map
        this.extendedData = extendedData
        this.overlay = overlay
        this.feature = null
        this.exportType = ExportType.Overlay

    }

    constructor(map: IMap,
                feature: IFeature<*>,
                extendedData: Boolean,
                callback: IEmpExportToTypeCallBack<File>,
                outputDirectory: String) {
        this.outputDirectory = File(outputDirectory)
        createOutputDirectory(outputDirectory)
        this.callback = callback
        this.map = map
        this.extendedData = extendedData
        this.overlay = null
        this.feature = feature
        this.exportType = ExportType.Feature
    }

    private fun createOutputDirectory(outputDirectory: String) {
        val directory = File(outputDirectory)
        directory.mkdirs()
        if (!directory.isDirectory) {
            throw IllegalArgumentException(String.format("The outputDirectory must be a path to a Directory. %s is not a directory.", outputDirectory))
        }

    }

    override fun run() {
        super.run()

        var kmlRelativePathExportThread: KMLRelativePathExportThread? = null

        when (this.exportType) {

            KMZExporterThread.ExportType.Map -> kmlRelativePathExportThread = KMLRelativePathExportThread(this.map,
                    this.extendedData,
                    object : IEmpExportToStringCallback {
                        override fun exportSuccess(stringFmt: String) {
                            CreateKMZfile(stringFmt, this@KMZExporterThread.outputDirectory)
                        }

                        override fun exportFailed(Ex: Exception) {

                        }
                    },
                    this.outputDirectory.absolutePath)
            KMZExporterThread.ExportType.Overlay -> kmlRelativePathExportThread = KMLRelativePathExportThread(this.map,
                    this.overlay,
                    this.extendedData,
                    object : IEmpExportToStringCallback {
                        override fun exportSuccess(stringFmt: String) {
                            CreateKMZfile(stringFmt, this@KMZExporterThread.outputDirectory)
                        }

                        override fun exportFailed(Ex: Exception) {
                            this@KMZExporterThread.callback.exportFailed(Ex)
                        }
                    },
                    this.outputDirectory.absolutePath)
            KMZExporterThread.ExportType.Feature -> kmlRelativePathExportThread = KMLRelativePathExportThread(this.map,
                    this.feature,
                    this.extendedData,
                    object : IEmpExportToStringCallback {
                        override fun exportSuccess(stringFmt: String) {
                            CreateKMZfile(stringFmt, this@KMZExporterThread.outputDirectory)
                        }

                        override fun exportFailed(Ex: Exception) {
                            this@KMZExporterThread.callback.exportFailed(Ex)
                        }
                    },
                    this.outputDirectory.absolutePath)
        }

        kmlRelativePathExportThread.run()
        kmlRelativePathExportThread.start()

    }

    private fun CreateKMZfile(kmlString: String, kmzDirectory: File) {

    }


}
