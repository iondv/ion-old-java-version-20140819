!macro Model_properties File_prop
  ClearErrors  
  Push $0
  FileOpen $0 ${File_prop} w 
  IfErrors errMP 
  
  FileWrite $0 "$\r$\npaths.metaDirectory=/meta"
  FileWrite $0 "$\r$\npaths.navigationDirectory=/navigation"
  FileWrite $0 "$\r$\npaths.viewDirectory=/views"
  FileWrite $0 "$\r$\npaths.validatorsDirectory=/validators"
  FileWrite $0 "$\r$\ndomain.package=$IONModelSmevPckg"

errMP:   
   FileClose $0 
   Pop $0
!macroend 