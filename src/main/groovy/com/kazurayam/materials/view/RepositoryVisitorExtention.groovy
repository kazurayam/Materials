package com.kazurayam.materials.view

import com.kazurayam.materials.ReportsAccessor
import com.kazurayam.materials.VisualTestingLogger

interface RepositoryVisitorExtended {
	
	void setReportsAccessor(ReportsAccessor reportsAccessor)
	
	void setVisualTestingLogger(VisualTestingLogger vtLogger)
}
