/* -*- Mode: Java; c-basic-offset: 4; tab-width: 4; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.scryer.detailpage

import android.graphics.Color
import android.graphics.PointF
import android.view.View
import android.view.ViewGroup
import androidx.collection.SparseArrayCompat
import androidx.viewpager.widget.PagerAdapter
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import org.mozilla.scryer.persistence.ScreenshotModel

class DetailPageAdapter : PagerAdapter() {

    var screenshots = listOf<ScreenshotModel>()
    var itemCallback: ItemCallback? = null
    var imageStateCallback: ImageStateCallback? = null

    private var pageViews = SparseArrayCompat<PageView>()

    override fun getCount(): Int {
        return screenshots.size
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val imageView = object : SubsamplingScaleImageView(container.context) {
            override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
                super.onSizeChanged(w, h, oldw, oldh)
                resetScaleAndCenter()
            }
        }

        val pageView = object : PageView {
            override fun getWidth(): Int {
                return imageView.width
            }

            override fun getHeight(): Int {
                return imageView.height
            }

            override fun getSourceImageWidth(): Int {
                return imageView.sWidth
            }

            override fun getSourceImageHeight(): Int {
                return imageView.sHeight
            }

            override fun resetScale() {
                imageView.resetScaleAndCenter()
            }

            override fun isScaled(): Boolean {
                return imageView.scale > imageView.minScale
            }
        }

        imageView.setOnStateChangedListener(object : SubsamplingScaleImageView.OnStateChangedListener {
            override fun onCenterChanged(newCenter: PointF, origin: Int) {}

            override fun onScaleChanged(newScale: Float, origin: Int) {
                imageStateCallback?.onScaleChanged(pageView)
            }
        })

        val item = screenshots[position]
        val path = item.absolutePath
//        Glide.with(container.context)
//                .asBitmap()
//                .load(path)
//                .into(object : SimpleTarget<Bitmap>() {
//                    override fun onLoadFailed(errorDrawable: Drawable?) {
//                        itemCallback?.onItemLoaded(item)
//                    }
//
//                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
//                        imageView.setImage(ImageSource.bitmap(resource))
//                        itemCallback?.onItemLoaded(item)
//                    }
//                })

        imageView.setImage(ImageSource.uri(path))
        imageView.setOnImageEventListener(object : SubsamplingScaleImageView.DefaultOnImageEventListener() {
            override fun onImageLoaded() {
                imageView.setBackgroundColor(Color.BLACK)
                imageView.resetScaleAndCenter()
            }
        })

        container.addView(imageView)
        imageView.setOnClickListener {
            itemCallback?.onItemClicked(screenshots[position])
        }

        pageViews.put(position, pageView)
        return imageView
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        container.removeView(obj as View)
        val view = pageViews[position]
        if (view == obj) {
            pageViews.remove(position)
        }
    }

    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return view == obj
    }

    fun findViewForPosition(position: Int): PageView? {
        return pageViews[position]
    }

    interface ItemCallback {
        fun onItemClicked(item: ScreenshotModel)
        fun onItemLoaded(item: ScreenshotModel)
    }

    interface ImageStateCallback {
        fun onScaleChanged(pageView: PageView)
    }

    interface PageView {
        fun resetScale()
        fun isScaled(): Boolean
        fun getSourceImageWidth(): Int
        fun getSourceImageHeight(): Int
        fun getWidth(): Int
        fun getHeight(): Int
    }
}
