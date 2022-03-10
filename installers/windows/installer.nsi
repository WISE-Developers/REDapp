Name "REDapp"
!define PRODUCT_NAME "REDapp"
!define PRODUCT_VERSION "${VER_PUBLIC}"
!define PRODUCT_WEB_SITE "http://www.REDapp.org"
!define /date MyTIMESTAMP "%Y%m%d"
!define PRODUCT_UNINST_KEY "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PRODUCT_NAME}"
; !define SKIP_ALL_FILES 1

!include "MUI2.nsh"
!include FileFunc.nsh

; The file to write
!ifdef OUTPUT_FILENAME
  OutFile "${OUTPUT_FILENAME}"
!else
  OutFile "REDapp-Windows-${MyTIMESTAMP}.exe"
!endif

; The default installation directory
InstallDir $PROGRAMFILES\REDapp

; Request application privileges for Windows Vista
RequestExecutionLevel admin

SetCompressor lzma

;--------------------------------
; Interface Settings

!define MUI_ABORTWARNING

!define MUI_LANGDLL_ALLLANGUAGES

!define MUI_ICON "..\bin\REDapp.ico"

;--------------------------------

; Pages

;Page welcome
!define MUI_WELCOMEFINISHPAGE_BITMAP "..\bin\redapplogo.bmp"
!insertmacro MUI_PAGE_WELCOME
;Page license
!insertmacro MUI_PAGE_LICENSE $(license)
; for accepting third party licenses
Page custom thirdPartyLicenseCreate
;Page components
!insertmacro MUI_PAGE_COMPONENTS
;Page directory
!insertmacro MUI_PAGE_DIRECTORY
;Page instfiles
!insertmacro MUI_PAGE_INSTFILES

; Uninstaller pages
!insertmacro MUI_UNPAGE_INSTFILES

!insertmacro MUI_LANGUAGE "English"
!insertmacro MUI_LANGUAGE "French"

LicenseLangString license ${LANG_ENGLISH} "..\bin\licence.rtf"
LicenseLangString license ${LANG_FRENCH} "..\bin\licence_fr.rtf"

;--------------------------------
;LicenseData "..\bin\Licence.txt"

; The stuff to install
Section "REDapp"

  SectionIn  RO
  
  ; Set output path to the installation directory.
  SetOutPath $INSTDIR
  
!ifndef SKIP_ALL_FILES
  ; Put file there
  File ..\bin\REDapp.jar
  File ..\bin\gpl-3.0.en.txt
  File ..\bin\REDapp.ico
!endif
  
  CreateDirectory $INSTDIR\REDapp_lib
  SetOutPath $INSTDIR\REDapp_lib
!ifndef SKIP_ALL_FILES
  File ..\bin\REDapp_lib\activation.jar
  File ..\bin\REDapp_Lib\appdirs.jar
  File ..\bin\REDapp_Lib\checker-qual.jar
  File ..\bin\REDapp_Lib\commons-codec.jar
  File ..\bin\REDapp_Lib\commons-collections4.jar
  File ..\bin\REDapp_lib\commons-compress.jar
  File ..\bin\REDapp_lib\commons-lang3.jar
  File ..\bin\REDapp_lib\commons-math3.jar
  File ..\bin\REDapp_lib\commons-pool.jar
  File ..\bin\REDapp_lib\commons-text.jar
  File ..\bin\REDapp_lib\curvesapi.jar
  File ..\bin\REDapp_lib\ejml-core.jar
  File ..\bin\REDapp_lib\ejml-ddense.jar
  File ..\bin\REDapp_lib\error_prone_annotations.jar
  File ..\bin\REDapp_lib\failureaccess.jar
  File ..\bin\REDapp_lib\Fuel.jar
  File ..\bin\REDapp_lib\FWI.jar
  File ..\bin\REDapp_lib\GeographicLib-Java.jar
  File ..\bin\REDapp_lib\Grid.jar
  File ..\bin\REDapp_lib\gson.jar
  File ..\bin\REDapp_lib\gt-main.jar
  File ..\bin\REDapp_lib\gt-metadata.jar
  File ..\bin\REDapp_lib\gt-opengis.jar
  File ..\bin\REDapp_lib\gt-referencing.jar
  File ..\bin\REDapp_lib\gt-shapefile.jar
  File ..\bin\REDapp_lib\guava.jar
  File ..\bin\REDapp_lib\hss-java.jar
  File ..\bin\REDapp_lib\istack-commons-runtime.jar
  File ..\bin\REDapp_lib\j2objc-annotations.jar
  File ..\bin\REDapp_Lib\jackson-annotations.jar
  File ..\bin\REDapp_Lib\jackson-core.jar
  File ..\bin\REDapp_Lib\jackson-databind.jar
  File ..\bin\REDapp_lib\jai_core.jar
  File ..\bin\REDapp_lib\jakarta.activation.jar
  File ..\bin\REDapp_lib\jakarta.xml.bind-api.jar
  File ..\bin\REDapp_lib\JavaApiforKml.jar
  File ..\bin\REDapp_lib\javaee-api.jar
  File ..\bin\REDapp_lib\javax.activation-api.jar
  File ..\bin\REDapp_lib\javax.mail.jar
  File ..\bin\REDapp_lib\jaxb-api.jar
  File ..\bin\REDapp_lib\jaxb-core.jar
  File ..\bin\REDapp_lib\jaxb-impl.jar
  File ..\bin\REDapp_lib\jaxb-runtime.jar
  File ..\bin\REDapp_lib\jaxb-xjc.jar
  File ..\bin\REDapp_lib\jdom2.jar
  File ..\bin\REDapp_lib\jgridshift-core.jar
  File ..\bin\REDapp_lib\jmapviewer.jar
  File ..\bin\REDapp_lib\jna.jar
  File ..\bin\REDapp_Lib\jna-platform.jar
  File ..\bin\REDapp_lib\josm.jar
  File ..\bin\REDapp_lib\jsr305.jar
  File ..\bin\REDapp_lib\jts-core.jar
  File ..\bin\REDapp_lib\listenablefuture.jar
  File ..\bin\REDapp_lib\logback-classic.jar
  File ..\bin\REDapp_lib\logback-core.jar
  File ..\bin\REDapp_lib\math.jar
  File ..\bin\REDapp_lib\poi.jar
  File ..\bin\REDapp_lib\poi-ooxml.jar
  File ..\bin\REDapp_lib\poi-ooxml-schemas.jar
  File ..\bin\REDapp_lib\protobuf-java.jar
  File ..\bin\REDapp_lib\protobuf-java-util.jar
  File ..\bin\REDapp_lib\REDapp_Lib.jar
  File ..\bin\REDapp_lib\si-quantity.jar
  File ..\bin\REDapp_lib\si-units-java8.jar
  File ..\bin\REDapp_Lib\slf4j-api.jar
  File ..\bin\REDapp_lib\SparseBitSet.jar
  File ..\bin\REDapp_lib\systems-common-java8.jar
  File ..\bin\REDapp_lib\txw2.jar
  File ..\bin\REDapp_lib\unit-api.jar
  File ..\bin\REDapp_lib\uom-lib-common.jar
  File ..\bin\REDapp_lib\uom-se.jar
  File ..\bin\REDapp_lib\versioncompare.jar
  File ..\bin\REDapp_lib\Weather.jar
  File ..\bin\REDapp_lib\WTime.jar
  File ..\bin\REDapp_lib\xml-apis.jar
  File ..\bin\REDapp_lib\xmlbeans.jar
!endif

  Delete "$INSTDIR\REDapp_Lib\animal-sniffer-annotations.jar"
  Delete "$INSTDIR\REDapp_Lib\commons-math3-3.3.jar"
  Delete "$INSTDIR\REDapp_Lib\commons-math3-3.6.jar"
  Delete "$INSTDIR\REDapp_Lib\commons-math3-3.6.1.jar"
  Delete "$INSTDIR\REDapp_Lib\commons-compress-1.10.jar"
  Delete "$INSTDIR\REDapp_Lib\commons-compress-1.15.jar"
  Delete "$INSTDIR\REDapp_lib\dom4j-1.6.1.jar"
  Delete "$INSTDIR\REDapp_lib\appdirs-1.0.1.jar"
  Delete "$INSTDIR\REDapp_lib\JavaFXBrowser.jar"
  Delete "$INSTDIR\REDapp_Lib\com.google.guava.guava-23.0.jar"
  Delete "$INSTDIR\REDapp_Lib\jna-4.0.0.jar"
  Delete "$INSTDIR\REDapp_Lib\jna-4.2.1.jar"
  Delete "$INSTDIR\REDapp_Lib\jna-4.5.0.jar"
  Delete "$INSTDIR\REDapp_Lib\poi-3.9-20121203.jar"
  Delete "$INSTDIR\REDapp_Lib\poi-3.13-20150929.jar"
  Delete "$INSTDIR\REDapp_Lib\poi-3.17.jar"
  Delete "$INSTDIR\REDapp_Lib\poi-ooxml-3.9-20121203.jar"
  Delete "$INSTDIR\REDapp_Lib\poi-ooxml-3.13-20150929.jar"
  Delete "$INSTDIR\REDapp_Lib\poi-ooxml-3.17.jar"
  Delete "$INSTDIR\REDapp_Lib\poi-ooxml-schemas-3.9-20121203.jar"
  Delete "$INSTDIR\REDapp_Lib\poi-ooxml-schemas-3.13-20150929.jar"
  Delete "$INSTDIR\REDapp_Lib\poi-ooxml-schemas-3.17.jar"
  Delete "$INSTDIR\REDapp_Lib\slf4j-jdk14-1.7.25.jar"
  Delete "$INSTDIR\REDapp_Lib\xmlbeans-2.3.0.jar"
  Delete "$INSTDIR\REDapp_Lib\swt_win32_x64.jar"
  Delete "$INSTDIR\REDapp_Lib\swt_win32_x86.jar"
  Delete "$INSTDIR\REDapp_Lib\slf4j-api-1.7.25.jar"
  Delete "$INSTDIR\REDapp_Lib\jaxb-xjc-2.2.4.jar"
  Delete "$INSTDIR\REDapp_Lib\jaxb-impl-2.2.4.jar"
  Delete "$INSTDIR\REDapp_Lib\DJNativeSwing.jar"
  Delete "$INSTDIR\REDapp_Lib\DJNativeSwing-SWT.jar"

  CreateDirectory $INSTDIR\html
  SetOutPath $INSTDIR\html
!ifndef SKIP_ALL_FILES
  File ..\bin\html\assumptions.htm
  File ..\bin\html\assumptions_fr.htm
  File ..\bin\html\assumptions2.htm
  File ..\bin\html\assumptions2_fr.htm
  File ..\bin\html\flag.png
  File ..\bin\html\flag.shadow.png
  File ..\bin\html\flame.png
  File ..\bin\html\fireperim.png
  File ..\bin\html\print.png
  File ..\bin\html\red-blank.png
  File ..\bin\html\red-dot.png
  File ..\bin\html\save.png
  File ..\bin\html\shadow.png
  File ..\bin\html\station_yellow.png
!endif
  
  Delete "$INSTDIR\html\blank.html"
  Delete "$INSTDIR\html\map.html"
  Delete "$INSTDIR\html\map_fr.html"
  Delete "$INSTDIR\html\Google_maps_logo.png"

  CreateDirectory $INSTDIR\html\assumptions_files
  SetOutPath $INSTDIR\html\assumptions_files
!ifndef SKIP_ALL_FILES
  File ..\bin\html\assumptions_files\colorschememapping.xml
  File ..\bin\html\assumptions_files\filelist.xml
  File ..\bin\html\assumptions_files\themedata.thmx
!endif
  
  SetOutPath $INSTDIR
  
SectionEnd ; end the section

Section "Desktop Shortcut"

  SetShellVarContext all
  CreateShortCut "$DESKTOP\REDapp.lnk" "$\"$INSTDIR\REDapp.jar$\"" "" "$INSTDIR\REDapp.ico"

SectionEnd

Section "Start Menu Shortcuts"

  SetShellVarContext all
  CreateDirectory "$SMPROGRAMS\REDapp"
  CreateShortCut "$SMPROGRAMS\REDapp\Uninstall.lnk" "$INSTDIR\uninstall.exe" "" "$INSTDIR\uninstall.exe" 0
  CreateShortCut "$SMPROGRAMS\REDapp\REDapp.lnk" "$\"$INSTDIR\REDapp.jar$\"" "" "$INSTDIR\REDapp.ico"

SectionEnd

Function thirdPartyLicenseCreate
  !insertmacro MUI_HEADER_TEXT "License Agreement" "Please review the license terms for third party libraries before installing ${PRODUCT_NAME}"

  GetDlgItem $R0 $HWNDPARENT 1
  SendMessage $R0 ${WM_SETTEXT} 0 `STR:I Agree`

  nsDialogs::Create 1018
  Pop $0
  ${If} $0 == error
    Abort
  ${EndIf}

  ${NSD_CreateLabel} 0 0 110u 10u "Apache Commons"
  Pop $0
  ${NSD_CreateLink} 115u 0 100% 10u "Apache 2.0"
  Pop $0
  ${NSD_OnClick} $0 onApacheLinkClick

  ${NSD_CreateLabel} 0 10u 110u 10u "poi"
  Pop $0
  ${NSD_CreateLink} 115u 10u 100% 10u "Apache 2.0"
  Pop $0
  ${NSD_OnClick} $0 onApacheLinkClick

  ${NSD_CreateLabel} 0 20u 110u 10u "HSS Libraries"
  Pop $0
  ${NSD_CreateLink} 115u 20u 100% 10u "Apache 2.0"
  Pop $0
  ${NSD_OnClick} $0 onApacheLinkClick

  ${NSD_CreateLabel} 0 30u 110u 10u "JOSM"
  Pop $0
  ${NSD_CreateLink} 115u 30u 100% 10u "GPLv3"
  Pop $0
  ${NSD_OnClick} $0 onGplv3LinkClick

  ${NSD_CreateLabel} 0 40u 110u 10u "OpenStreetMaps"
  Pop $0
  ${NSD_CreateLink} 115u 40u 100% 10u "Open Database License"
  Pop $0
  ${NSD_OnClick} $0 onOdcLinkClick

  ${NSD_CreateLabel} 0 50u 110u 10u "AppDirs"
  Pop $0
  ${NSD_CreateLink} 115u 50u 100% 10u "Apache 2.0"
  Pop $0
  ${NSD_OnClick} $0 onApacheLinkClick

  ${NSD_CreateLabel} 0 60u 110u 10u "Faster XML Jackson"
  Pop $0
  ${NSD_CreateLink} 115u 60u 100% 10u "Apache 2.0"
  Pop $0
  ${NSD_OnClick} $0 onApacheLinkClick

  nsDialogs::Show
FunctionEnd

Function onApacheLinkClick
  Pop $0
  ExecShell "open" "https://www.apache.org/licenses/LICENSE-2.0.txt"
FunctionEnd

Function onGplv3LinkClick
  Pop $0
  ExecShell "open" "https://www.apache.org/licenses/LICENSE-2.0.txt"
FunctionEnd

Function onOdcLinkClick
  Pop $0
  ExecShell "open" "https://www.apache.org/licenses/LICENSE-2.0.txt"
FunctionEnd

Section -Post
  WriteRegStr SHCTX "${PRODUCT_UNINST_KEY}" "DisplayName" "$(^Name)"
  WriteREgStr SHCTX "${PRODUCT_UNINST_KEY}" "UninstallString" "$\"$INSTDIR\Uninstall.exe$\""
  WriteRegStr SHCTX "${PRODUCT_UNINST_KEY}" "QuietUninstallString" "$\"$INSTDIR\Uninstall.exe$\" /S"
  WriteRegStr SHCTX "${PRODUCT_UNINST_KEY}" "DisplayIcon" "$\"$INSTDIR\REDapp.ico$\""
  WriteRegStr SHCTX "${PRODUCT_UNINST_KEY}" "DisplayVersion" "${PRODUCT_VERSION}"
  WriteRegStr SHCTX "${PRODUCT_UNINST_KEY}" "URLInfoAbout" "${PRODUCT_WEB_SITE}"
  ${GetSize} "$INSTDIR" "/S=OK" $0 $1 $2
  IntFmt $0 "0x%08X" $0
  WriteRegDWORD SHCTX "${PRODUCT_UNINST_KEY}" "EstimatedSize" "$0"
SectionEnd

Section "Uninstall"
  
  Delete "$INSTDIR\REDapp.jar"
  Delete "$INSTDIR\Readme.txt"
  Delete "$INSTDIR\Licence.txt"
  Delete "$INSTDIR\REDapp.ico"
  Delete "$INSTDIR\gpl-3.0.en.txt"
		  
  RMDir /r "$INSTDIR\REDapp_lib"

  Delete "$INSTDIR\html\assumptions_files\colorschememapping.xml"
  Delete "$INSTDIR\html\assumptions_files\filelist.xml"
  Delete "$INSTDIR\html\assumptions_files\themedata.thmx"
  RMDir "$INSTDIR\html\assumptions_files"

  Delete "$INSTDIR\html\assumptions.htm"
  Delete "$INSTDIR\html\assumptions_fr.htm"
  Delete "$INSTDIR\html\assumptions2.htm"
  Delete "$INSTDIR\html\assumptions2_fr.htm"
  Delete "$INSTDIR\html\blank.html"
  Delete "$INSTDIR\html\map.html"
  Delete "$INSTDIR\html\map_fr.html"
  Delete "$INSTDIR\html\flag.png"
  Delete "$INSTDIR\html\flag.shadow.png"
  Delete "$INSTDIR\html\flame.png"
  Delete "$INSTDIR\html\fireperim.png"
  Delete "$INSTDIR\html\Google_maps_logo.png"
  Delete "$INSTDIR\html\print.png"
  Delete "$INSTDIR\html\red-blank.png"
  Delete "$INSTDIR\html\red-dot.png"
  Delete "$INSTDIR\html\save.png"
  Delete "$INSTDIR\html\shadow.png"
  Delete "$INSTDIR\html\station_yellow.png"
  RMDir "$INSTDIR\html"

  SetShellVarContext all
  Delete "$INSTDIR\Uninstall.exe"
  RMDir "$INSTDIR"
  Delete "$SMPROGRAMS\REDapp\Uninstall.lnk"
  Delete "$SMPROGRAMS\REDapp\REDapp.lnk"
  RMDir "$SMPROGRAMS\REDapp"
  Delete "$DESKTOP\REDapp.lnk"

  DeleteRegKey SHCTX "${PRODUCT_UNINST_KEY}"

SectionEnd


