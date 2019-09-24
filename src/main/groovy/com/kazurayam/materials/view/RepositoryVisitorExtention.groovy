package com.kazurayam.materials.view

import com.kazurayam.materials.ReportsAccessor
import com.kazurayam.materials.VisualTestingLogger

interface RepositoryVisitorExtention {
	
	void setReportsAccessor(ReportsAccessor reportsAccessor)
	
	void setVisualTestingLogger(VisualTestingLogger vtLogger)
}
