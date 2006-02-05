package org.opennms.mavenize;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.opennms.mavenize.config.Dependencies;
import org.opennms.mavenize.config.Dependency;
import org.opennms.mavenize.config.Exclude;
import org.opennms.mavenize.config.Fileset;
import org.opennms.mavenize.config.Include;
import org.opennms.mavenize.config.Module;
import org.opennms.mavenize.config.ModuleDependency;
import org.opennms.mavenize.config.Project;
import org.opennms.mavenize.config.Sources;

public class ProjectBuildingVisitor extends AbstractSpecVisitor {
	
	LinkedList m_builders = new LinkedList();

	public ProjectBuildingVisitor(PomBuilder builder) {
		m_builders.addFirst(builder);
	}
	
	public void visitProject(Project project) {
		getBuilder().setGroupId(project.getGroupId());
		getBuilder().setArtifactId(project.getArtifactId());
		getBuilder().setVersion(project.getVersion());
		getBuilder().setName(project.getName());
		getBuilder().setPackaging(project.getPackaging());
	}
	

	public void visitModule(Module module) {
		PomBuilder modBuilder = getBuilder().createModule(module.getModuleId());
		pushBuilder(modBuilder);
		getBuilder().setName(module.getModuleName());
		getBuilder().setPackaging(module.getModuleType());
	}
	
	public void completeModule(Module module) {
		popBuilder();
	}

	public void visitSources(Sources souces) {
		// TODO Auto-generated method stub

	}

	public void visitFileSet(Fileset fileset) {
		// TODO Auto-generated method stub

	}

	public void visitInclude(Include include) {
		// TODO Auto-generated method stub

	}

	public void visitExclude(Exclude exclude) {
		// TODO Auto-generated method stub

	}

	public void visitDependencies(Dependencies deps) {
		// TODO Auto-generated method stub

	}

	public void visitDependency(Dependency dep) {
		getBuilder().addDependency(dep.getGroupId(), dep.getArtifactId(), dep.getVersion(), dep.getScope());
	}

	public void visitModuleDependency(ModuleDependency modDep) {
		getBuilder().addDependency(getBuilder().getGroupId(), modDep.getModuleId(), getBuilder().getVersion(), modDep.getScope());
	}

	void pushBuilder(PomBuilder builder) {
		m_builders.addFirst(builder);
	}
	
	PomBuilder popBuilder() {
		return (PomBuilder)m_builders.removeFirst();
	}
	

	PomBuilder getBuilder() {
		return (PomBuilder)m_builders.getFirst();
	}

}
