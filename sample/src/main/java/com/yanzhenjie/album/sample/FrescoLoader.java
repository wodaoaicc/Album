/*
 * Copyright 2017 Yan Zhenjie.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.yanzhenjie.album.sample;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.facebook.common.executors.UiThreadImmediateExecutorService;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.github.piasy.biv.view.BigImageView;
import com.github.piasy.biv.view.FrescoImageViewFactory;
import com.yanzhenjie.album.AlbumFile;
import com.yanzhenjie.album.AlbumLoader;
import com.yanzhenjie.album.sample.photoview.AttacherImageView;
import com.yanzhenjie.album.sample.photoview.PhotoViewAttacher;

/**
 * Created by Yan Zhenjie on 2017/3/31.
 */
public class FrescoLoader implements AlbumLoader {
    
    @Override
    public void load(ImageView imageView, AlbumFile albumFile) {
        load(imageView, "file://" + albumFile.getPath());
    }
    
    @Override
    public void load(ImageView imageView, String url) {
        if (imageView instanceof SimpleDraweeView) {
            ((SimpleDraweeView) imageView).setImageURI(url);
        }
    }
    
    @NonNull
    @Override
    public View getPreviewView(Context context, AlbumFile albumFile, final View.OnClickListener onClickListener, final View.OnClickListener longClickListener) {
        if (albumFile.getMediaType() == AlbumFile.TYPE_VIDEO) {
            final AttacherImageView view = new AttacherImageView(context);
            view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            DataSource<CloseableReference<CloseableImage>> dataSource = Fresco.getImagePipeline()
                .fetchDecodedImage(ImageRequest.fromUri("file://" + albumFile.getPath()), context);
            dataSource.subscribe(new BaseBitmapDataSubscriber() {
                @Override
                protected void onNewResultImpl(Bitmap bitmap) {
                    view.setImageDrawable(new BitmapDrawable(bitmap.copy(bitmap.getConfig(), bitmap.isMutable())));
                }
                
                @Override
                protected void onFailureImpl(DataSource<CloseableReference<CloseableImage>> dataSource) {
                }
            }, UiThreadImmediateExecutorService.getInstance());
            final PhotoViewAttacher attacher = new PhotoViewAttacher(view);
            if (onClickListener != null) {
                attacher.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
                    @Override
                    public void onViewTap(View v, float x, float y) {
                        onClickListener.onClick(v);
                    }
                });
            }
            if (longClickListener != null) {
                attacher.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        longClickListener.onClick(v);
                        return true;
                    }
                });
            }
            view.setAttacher(attacher);
            return view;
        } else {
            return getPreviewView(context, "file://" + albumFile.getPath(), onClickListener, longClickListener);
        }
    }
    
    @NonNull
    @Override
    public View getPreviewView(Context context, String url, View.OnClickListener onClickListener, final View.OnClickListener longClickListener) {
        final BigImageView view = new BigImageView(context);
        view.setImageViewFactory(new FrescoImageViewFactory());
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        view.showImage(Uri.parse(url));
        view.setOnClickListener(onClickListener);
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                longClickListener.onClick(view);
                return true;
            }
        });
        return view;
    }
}