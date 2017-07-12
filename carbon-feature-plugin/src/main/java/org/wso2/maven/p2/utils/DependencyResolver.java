/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.maven.p2.utils;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.project.MavenProject;
import org.apache.maven.repository.RepositorySystem;
import org.wso2.maven.p2.beans.CarbonArtifact;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * DependencyResolver takes MavenProject object and resolve all the maven dependencies in the maven project into
 * internal bean representations.
 *
 * @since 2.0.0
 */
public class DependencyResolver {

    /**
     * Resolve the given project dependencies into CarbonArtifact objects. Dependencies are categorized into
     * OSGI bundles and Carbon features.
     *
     * @param project            MavenProject  Maven Project
     * @param repositorySystem   RepositorySystem object
     * @param remoteRepositories collection of remote repositories
     * @param localRepository    local repository representation
     * @return Return a {@code List<HashMap<String, CarbonArtifact>>}, 1st item being {@code HashMap<String,
     * CarbonArtifact>} containing osgi bundles specified as dependencies and 2nd item being {@code HashMap<String,
     * CarbonArtifact>} containing carbon features specified as dependencies.
     * @throws IOException throws when unable to retrieve a given maven artifact
     */
    public static List<HashMap<String, CarbonArtifact>> getDependenciesForProject(MavenProject project, RepositorySystem
            repositorySystem, List<ArtifactRepository> remoteRepositories, ArtifactRepository localRepository)
            throws IOException {

        List<HashMap<String, CarbonArtifact>> results = new ArrayList<>();
        HashMap<String, CarbonArtifact> bundles = new HashMap<>();
        HashMap<String, CarbonArtifact> features = new HashMap<>();
        results.add(bundles);   //references of bundle and features ( refering to maps ) are added to the
        // ArrayList refereced by results
        results.add(features);
        List<Dependency> dependencies = project.getDependencies();

        DependencyManagement dependencyManagement = project.getDependencyManagement();
        if (dependencyManagement != null) {
            dependencies.addAll(dependencyManagement.getDependencies());
        }
        for (Dependency dependency : dependencies) {
            CarbonArtifact carbonArtifact = new CarbonArtifact();
            carbonArtifact.setGroupId(dependency.getGroupId());
            carbonArtifact.setArtifactId(dependency.getArtifactId());
            carbonArtifact.setVersion(dependency.getVersion());
            carbonArtifact.setType(dependency.getType());
            Artifact mavenArtifact = MavenUtils.getResolvedArtifact(carbonArtifact, repositorySystem,
                    remoteRepositories, localRepository);
            carbonArtifact.setArtifact(mavenArtifact);
            String key;
            if (carbonArtifact.getType().equals("jar")) {
                if (resolveOSGIInfo(carbonArtifact)) {
                    key = carbonArtifact.getSymbolicName() + "_" + carbonArtifact.getBundleVersion();
                    bundles.put(key, carbonArtifact);
                }
            } else {
                key = carbonArtifact.getArtifactId() + "_" + carbonArtifact.getVersion();
                features.put(key, carbonArtifact);
            }
        }
        return results; // return the reference to the list having references to the both the map
        // s having features and bundles
    }

    /**
     * Resolves OSGi information for a given {@link CarbonArtifact} and populate OSGi information.
     *
     * @param artifact {@link CarbonArtifact}
     * @return {@code boolean} indicating whether the OSGi information is successfully resolved or not
     * @throws IOException if unable to retrieve the maven artifact represented by the given {@link CarbonArtifact}
     */
    private static boolean resolveOSGIInfo(CarbonArtifact artifact) throws IOException {
        String bundleVersionStr = "Bundle-Version";
        String bundleSymbolicNameStr = "Bundle-SymbolicName";

        if (!artifact.getArtifact().getFile().exists()) {
            return false;
        }
        try (JarFile jarFile = new JarFile(artifact.getArtifact().getFile())) {

            Manifest manifest = jarFile.getManifest();
            if (manifest == null) {
                return false;
            }
            String bundleSymbolicName = manifest.getMainAttributes().getValue(bundleSymbolicNameStr);
            String bundleVersion = manifest.getMainAttributes().getValue(bundleVersionStr);
            //Returns false if the considered .jar is not an OSGI bundle
            if (bundleSymbolicName == null || bundleVersion == null) {
                return false;
            }
            String[] split = bundleSymbolicName.split(";");
            artifact.setSymbolicName(split[0]);
            artifact.setBundleVersion(bundleVersion);
            return true;
        } catch (IOException e) {
            throw new IOException("Unable to retrieve maven artifact: " + artifact.getGroupId() +
                    ":" + artifact.getArtifactId() + ":" + artifact.getVersion(), e);
        }
    }
}
