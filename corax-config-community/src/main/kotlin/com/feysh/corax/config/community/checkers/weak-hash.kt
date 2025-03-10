/*
 *  CoraxJava - a Java Static Analysis Framework
 *  Copyright (C) 2024.  Feysh-Tech Group
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.feysh.corax.config.community.checkers

import com.feysh.corax.config.api.AIAnalysisApi
import com.feysh.corax.config.api.AIAnalysisUnit
import com.feysh.corax.config.api.IOperatorFactory
import com.feysh.corax.config.api.IStringExpr
import com.feysh.corax.config.api.baseimpl.matchSimpleSig
import com.feysh.corax.config.api.baseimpl.matchStaticMethod
import com.feysh.corax.config.api.PreAnalysisApi
import com.feysh.corax.config.api.PreAnalysisUnit
import com.feysh.corax.config.general.checkers.GeneralTaintTypes
import com.feysh.corax.config.community.WeakHashChecker
import java.security.MessageDigest
import java.security.Provider
import java.security.Signature


@Suppress("ClassName")
object `weak-hash` : AIAnalysisUnit() {
    const val apache = "org.apache.commons.codec.digest.DigestUtils"
    const val hutool = "cn.hutool.crypto.digest.DigestUtil"
    const val spring = "org.springframework.util.DigestUtils"
    private fun IOperatorFactory.isShaHash(algorithm: IStringExpr) =
        algorithm.stringEquals("sha1") or algorithm.stringEquals("sha-1") or algorithm.stringEquals("sha")
    private fun IOperatorFactory.isMdHash(algorithm: IStringExpr) =
        algorithm.stringEquals("md2") or algorithm.stringEquals("md4") or algorithm.stringEquals("md5")
    context (AIAnalysisApi)
    override suspend fun config() {

        listOf(
            matchStaticMethod<MessageDigest, String>(MessageDigest::getInstance),
            matchStaticMethod<MessageDigest, String, String>(MessageDigest::getInstance),
            matchStaticMethod<MessageDigest, String, Provider>(MessageDigest::getInstance),
            matchSimpleSig("$apache: * getDigest(**)"),
            matchSimpleSig("$hutool: cn.hutool.crypto.digest.Digester digester(java.lang.String)"),
            matchStaticMethod<Signature, String>(Signature::getInstance),
            matchStaticMethod<Signature, String, String>(Signature::getInstance),
            matchStaticMethod<Signature, String, Provider>(Signature::getInstance),
        ).forEach {
            method(it).modelNoArg {
                val algorithm = p0.getString()
                val algorithmLower = algorithm.toLowerCase()
                check(isShaHash(algorithmLower), WeakHashChecker.WeakMessageDigestSha1)
                check(p0.taint.containsAll(taintOf(GeneralTaintTypes.Untrusted)), WeakHashChecker.WeakMessageDigestSha1)

                check(isMdHash(algorithmLower), WeakHashChecker.WeakMessageDigestMd5)
                check(p0.taint.containsAll(taintOf(GeneralTaintTypes.Untrusted)), WeakHashChecker.WeakMessageDigestMd5)
            }
        }

        method(matchSimpleSig("$hutool: cn.hutool.crypto.digest.Digester digester(cn.hutool.crypto.digest.DigestAlgorithm)"))
            .modelNoArg {
                check(isMdHash(p0.getEnumName().toLowerCase()), WeakHashChecker.WeakMessageDigestMd5)
            }
        method(matchSimpleSig("$hutool: cn.hutool.crypto.digest.Digester digester(cn.hutool.crypto.digest.DigestAlgorithm)"))
            .modelNoArg {
                check(isShaHash(p0.getEnumName().toLowerCase()), WeakHashChecker.WeakMessageDigestSha1)
            }

    }



    object `api-call` : PreAnalysisUnit() {


        context (PreAnalysisApi)
        override suspend fun config() {

            atAnyClass {
                if (superClasses.any { it.name == "java.security.MessageDigest" }){
                    report(WeakHashChecker.CustomMessageDigest)
                }
            }


            listOf(
                matchSimpleSig("$apache: * getSha1Digest(**)"),
                matchSimpleSig("$apache: * getShaDigest(**)"),
                matchSimpleSig("$apache: * sha1(**)"),
                matchSimpleSig("$apache: * sha(**)"),
                matchSimpleSig("$apache: * sha1Hex(**)"),
                matchSimpleSig("$apache: * shaHex(**)"),
                matchSimpleSig("$hutool: * sha1(**)"),
            ).forEach {
                atInvoke(it){ report(WeakHashChecker.WeakMessageDigestSha1) }
            }
            listOf(
                matchSimpleSig("$apache: * getMd2Digest(**)"),
                matchSimpleSig("$apache: * md2(**)"),
                matchSimpleSig("$apache: * md2Hex(**)"),
                matchSimpleSig("$apache: * getMd5Digest(**)"),
                matchSimpleSig("$apache: * md5(**)"),
                matchSimpleSig("$apache: * md5Hex(**)"),

                matchSimpleSig("$hutool: * md2(**)"),
                matchSimpleSig("$hutool: * md5(**)"),

                matchSimpleSig("$spring: * md5DigestAsHex(**)"),
                matchSimpleSig("$spring: * appendMd5DigestAsHex(**)"),
                matchSimpleSig("$spring: * md5Digest(**)"),
            ).forEach {
                atInvoke(it) { report(WeakHashChecker.WeakMessageDigestMd5) }
            }
        }

    }
}