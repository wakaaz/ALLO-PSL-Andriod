package com.net.pslapllication.util.customvideoview

import android.annotation.TargetApi;
import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.net.Uri;
import android.os.Build;

import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;


class MediaExtractor {

    private var mApiExtractor: MediaExtractor = renewExtractor()



    protected fun renewExtractor() :MediaExtractor{
        if (mApiExtractor != null) {
            mApiExtractor.release()
        }else{
            mApiExtractor = MediaExtractor()
        }

        return mApiExtractor
    }

    /**
     * Sets the data source as a content Uri.
     *
     * @param context the Context to use when resolving the Uri
     * @param uri the Content URI of the data you want to extract from.
     * @param headers the headers to be sent together with the request for the data
     */
    @Throws(IOException::class)
    fun setDataSource(
        context: Context?,
        uri: Uri?,
        headers: Map<String, String>?
    ) {
        mApiExtractor!!.setDataSource(context, uri, headers)
    }

    /**
     * Sets the data source (file-path or http URL) to use.
     *
     * @param path the path of the file, or the http URL
     * @param headers the headers associated with the http request for the stream you want to play
     */
    @Throws(IOException::class)
    fun setDataSource(
        path: String?,
        headers: Map<String?, String?>?
    ) {
        mApiExtractor!!.setDataSource(path!!, headers)
    }

    /**
     * Sets the data source (file-path or http URL) to use.
     *
     * @param path the path of the file, or the http URL of the stream
     *
     *
     * When `path` refers to a local file, the file may actually be opened by a
     * process other than the calling application.  This implies that the pathname
     * should be an absolute path (as any other process runs with unspecified current working
     * directory), and that the pathname should reference a world-readable file.
     * As an alternative, the application could first open the file for reading,
     * and then use the file descriptor form [.setDataSource].
     */
    @Throws(IOException::class)
    fun setDataSource(path: String?) {
        mApiExtractor!!.setDataSource(path!!)
    }

    /**
     * Sets the data source (FileDescriptor) to use. It is the caller's responsibility
     * to close the file descriptor. It is safe to do so as soon as this call returns.
     *
     * @param fd the FileDescriptor for the file you want to extract from.
     */
    @Throws(IOException::class)
    fun setDataSource(fd: FileDescriptor?) {
        mApiExtractor!!.setDataSource(fd)
    }

    /**
     * Sets the data source (FileDescriptor) to use.  The FileDescriptor must be
     * seekable (N.B. a LocalSocket is not seekable). It is the caller's responsibility
     * to close the file descriptor. It is safe to do so as soon as this call returns.
     *
     * @param fd the FileDescriptor for the file you want to extract from.
     * @param offset the offset into the file where the data to be extracted starts, in bytes
     * @param length the length in bytes of the data to be extracted
     */
    @Throws(IOException::class)
    fun setDataSource(
        fd: FileDescriptor?, offset: Long, length: Long
    ) {
        mApiExtractor!!.setDataSource(fd, offset, length)
    }

    /**
     * Make sure you call this when you're done to free up any resources
     * instead of relying on the garbage collector to do this for you at
     * some point in the future.
     */
    fun release() {
        mApiExtractor!!.release()
    }

    /**
     * Count the number of tracks found in the data source.
     */
    val trackCount: Int
        get() = mApiExtractor!!.trackCount

    /**
     * Get the PSSH info if present.
     * @return a map of uuid-to-bytes, with the uuid specifying
     * the crypto scheme, and the bytes being the data specific to that scheme.
     */
    @get:TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    val psshInfo: Map<Any, ByteArray>?
        get() = mApiExtractor!!.psshInfo

    /**
     * Get the track format at the specified index.
     * More detail on the representation can be found at [android.media.MediaCodec]
     */
    fun getTrackFormat(index: Int): MediaFormat {
        val mediaFormat = mApiExtractor!!.getTrackFormat(index)
        val mime = mediaFormat.getString(MediaFormat.KEY_MIME)

        // Set the default DAR
        //
        // We need to check the existence of the width/height fields because some platforms
        // return unsupported tracks as "video/unknown" mime type without the required fields to
        // calculate the DAR.
        //
        // Example:
        // Samsung Galaxy S5 Android 6.0.1 with thumbnail tracks (jpeg image)
        // MediaFormat{error-type=-1002, mime=video/unknown, isDMCMMExtractor=1, durationUs=323323000}
        if (mime!!.startsWith("video/")
            && mediaFormat.containsKey(MediaFormat.KEY_WIDTH)
            && mediaFormat.containsKey(MediaFormat.KEY_HEIGHT)
        ) {
            mediaFormat.setFloat(
                MEDIA_FORMAT_EXTENSION_KEY_DAR,
                mediaFormat.getInteger(MediaFormat.KEY_WIDTH).toFloat()
                        / mediaFormat.getInteger(MediaFormat.KEY_HEIGHT)
            )
        }
        return mediaFormat
    }

    /**
     * Subsequent calls to [.readSampleData], [.getSampleTrackIndex] and
     * [.getSampleTime] only retrieve information for the subset of tracks
     * selected.
     * Selecting the same track multiple times has no effect, the track is
     * only selected once.
     */
    fun selectTrack(index: Int) {
        mApiExtractor!!.selectTrack(index)
    }

    /**
     * Subsequent calls to [.readSampleData], [.getSampleTrackIndex] and
     * [.getSampleTime] only retrieve information for the subset of tracks
     * selected.
     */
    fun unselectTrack(index: Int) {
        mApiExtractor!!.unselectTrack(index)
    }

    /**
     * All selected tracks seek near the requested time according to the
     * specified mode.
     */
    @Throws(IOException::class)
    fun seekTo(timeUs: Long, mode: Int) {
        mApiExtractor!!.seekTo(timeUs, mode)
    }

    /**
     * Advance to the next sample. Returns false if no more sample data
     * is available (end of stream).
     */
    fun advance(): Boolean {
        return mApiExtractor!!.advance()
    }

    /**
     * Retrieve the current encoded sample and store it in the byte buffer
     * starting at the given offset. Returns the sample size (or -1 if
     * no more samples are available).
     */
    fun readSampleData(byteBuf: ByteBuffer?, offset: Int): Int {
        return mApiExtractor!!.readSampleData(byteBuf, offset)
    }

    /**
     * Returns the track index the current sample originates from (or -1
     * if no more samples are available)
     */
    val sampleTrackIndex: Int
        get() = mApiExtractor!!.sampleTrackIndex

    /**
     * Returns the current sample's presentation time in microseconds.
     * or -1 if no more samples are available.
     */
    val sampleTime: Long
        get() = mApiExtractor!!.sampleTime

    /**
     * Returns the current sample's flags.
     */
    val sampleFlags: Int
        get() = mApiExtractor!!.sampleFlags

    /**
     * If the sample flags indicate that the current sample is at least
     * partially encrypted, this call returns relevant information about
     * the structure of the sample data required for decryption.
     * @param info The android.media.MediaCodec.CryptoInfo structure
     * to be filled in.
     * @return true iff the sample flags contain [.SAMPLE_FLAG_ENCRYPTED]
     */
    fun getSampleCryptoInfo(info: MediaCodec.CryptoInfo?): Boolean {
        return mApiExtractor!!.getSampleCryptoInfo(info!!)
    }

    /**
     * Returns an estimate of how much data is presently cached in memory
     * expressed in microseconds. Returns -1 if that information is unavailable
     * or not applicable (no cache).
     */
    val cachedDuration: Long
        get() = mApiExtractor!!.cachedDuration

    /**
     * Returns true iff we are caching data and the cache has reached the
     * end of the data stream (for now, a future seek may of course restart
     * the fetching of data).
     * This API only returns a meaningful result if [.getCachedDuration]
     * indicates the presence of a cache, i.e. does NOT return -1.
     */
    fun hasCacheReachedEndOfStream(): Boolean {
        return mApiExtractor!!.hasCacheReachedEndOfStream()
    }

    /**
     * Returns true iff the extracted media supports intra-stream switching of formats (e.g. resolution)
     * and the format has changed. It only returns true at the first call when the format has changed,
     * then returns false until it has changed again.
     */
    fun hasTrackFormatChanged(): Boolean {
        return false
    }

    companion object {
        const val MEDIA_FORMAT_EXTENSION_KEY_DAR = "mpx-dar"

        /**
         * If possible, seek to a sync sample at or before the specified time
         */
        const val SEEK_TO_PREVIOUS_SYNC = 0

        /**
         * If possible, seek to a sync sample at or after the specified time
         */
        const val SEEK_TO_NEXT_SYNC = 1

        /**
         * If possible, seek to the sync sample closest to the specified time
         */
        const val SEEK_TO_CLOSEST_SYNC = 2
        // Keep these in sync with their equivalents in NuMediaExtractor.h
        /**
         * The sample is a sync sample
         */
        const val SAMPLE_FLAG_SYNC = 1

        /**
         * The sample is (at least partially) encrypted, see also the documentation
         * for [android.media.MediaCodec.queueSecureInputBuffer]
         */
        const val SAMPLE_FLAG_ENCRYPTED = 2
    }

init {

}
}