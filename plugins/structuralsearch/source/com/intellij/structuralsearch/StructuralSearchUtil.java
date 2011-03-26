package com.intellij.structuralsearch;

import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.fileTypes.FileTypes;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.fileTypes.impl.AbstractFileType;
import com.intellij.psi.PsiElement;
import com.intellij.tokenindex.LanguageTokenizer;
import com.intellij.tokenindex.Tokenizer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author Eugene.Kudelevsky
 */
public class StructuralSearchUtil {
  private static final List<StructuralSearchProfile> myRegisteredProfiles  = new ArrayList<StructuralSearchProfile>();
  public static final FileType DEFAULT_FILE_TYPE = StdFileTypes.JAVA;

  public static boolean ourUseUniversalMatchingAlgorithm = false;

  private static final StructuralSearchProfile ourUniversalMatchingProfile = new StructuralSearchProfileImpl();

  static {
    Collections
      .addAll(myRegisteredProfiles, new JavaStructuralSearchProfile(), new XmlStructuralSearchProfile(), ourUniversalMatchingProfile);
  }

  private StructuralSearchUtil() {
  }

  @Nullable
  public static StructuralSearchProfile getProfileByPsiElement(@NotNull PsiElement element) {
    return getProfileByLanguage(element.getLanguage());
  }

  @Nullable
  public static StructuralSearchProfile getProfileByLanguage(@NotNull Language language) {
    if (ourUseUniversalMatchingAlgorithm && ourUniversalMatchingProfile.isMyLanguage(language)) {
      return ourUniversalMatchingProfile;
    }

    for (StructuralSearchProfile profile : StructuralSearchProfile.EP_NAME.getExtensions()) {
      if (profile.isMyLanguage(language)) {
        return profile;
      }
    }
    for (StructuralSearchProfile profile : myRegisteredProfiles) {
      if (profile.isMyLanguage(language)) {
        return profile;
      }
    }

    if (ourUniversalMatchingProfile.isMyLanguage(language)) {
      return ourUniversalMatchingProfile;
    }
    return null;
  }

  public static List<StructuralSearchProfile> getAllProfiles() {
    List<StructuralSearchProfile> profiles = new ArrayList<StructuralSearchProfile>();
    Collections.addAll(profiles, StructuralSearchProfile.EP_NAME.getExtensions());
    profiles.addAll(myRegisteredProfiles);
    return profiles;
  }

  @Nullable
  public static Tokenizer getTokenizerForLanguage(@NotNull Language language) {
    return LanguageTokenizer.INSTANCE.forLanguage(language);
  }

  public static boolean isTypedVariable(@NotNull final String name) {
    return name.charAt(0)=='$' && name.charAt(name.length()-1)=='$';
  }

  @Nullable
  public static StructuralSearchProfile getProfileByFileType(FileType fileType) {
    if (ourUseUniversalMatchingAlgorithm && ourUniversalMatchingProfile.canProcess(fileType)) {
      return ourUniversalMatchingProfile;
    }

    for (StructuralSearchProfile profile : StructuralSearchProfile.EP_NAME.getExtensions()) {
      if (profile.canProcess(fileType)) {
        return profile;
      }
    }
    for (StructuralSearchProfile profile : myRegisteredProfiles) {
      if (profile.canProcess(fileType)) {
        return profile;
      }
    }

    if (ourUniversalMatchingProfile.canProcess(fileType)) {
      return ourUniversalMatchingProfile;
    }
    return null;
  }

  @NotNull
  public static FileType[] getSuitableFileTypes() {
    Set<FileType> allFileTypes = new HashSet<FileType>();
    Collections.addAll(allFileTypes, FileTypeManager.getInstance().getRegisteredFileTypes());
    for (Language language : Language.getRegisteredLanguages()) {
      FileType fileType = language.getAssociatedFileType();
      if (fileType != null) {
        allFileTypes.add(fileType);
      }
    }

    List<FileType> result = new ArrayList<FileType>();
    for (FileType fileType : allFileTypes) {
      if (fileType != StdFileTypes.GUI_DESIGNER_FORM &&
          fileType != StdFileTypes.IDEA_MODULE &&
          fileType != StdFileTypes.IDEA_PROJECT &&
          fileType != StdFileTypes.IDEA_WORKSPACE &&
          fileType != FileTypes.ARCHIVE &&
          fileType != FileTypes.UNKNOWN &&
          fileType != FileTypes.PLAIN_TEXT &&
          !(fileType instanceof AbstractFileType) &&
          !fileType.isBinary() &&
          !fileType.isReadOnly()) {
        result.add(fileType);
      }
    }

    return result.toArray(new FileType[result.size()]);
  }
}
