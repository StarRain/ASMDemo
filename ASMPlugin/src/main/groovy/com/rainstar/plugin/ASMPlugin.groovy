package com.rainstar.plugin

import com.android.build.gradle.AppExtension
import com.rainstar.util.LogUtil
import org.gradle.api.Plugin
import org.gradle.api.Project

class ASMPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        LogUtil.log("ASMPlugin apply start.")
        AppExtension appExtension = project.getExtensions().findByType(AppExtension)
        appExtension.registerTransform(new ASMTransform())
        LogUtil.log("ASMPlugin apply end.")
    }
}

