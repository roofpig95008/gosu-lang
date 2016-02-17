package gw.lang.gosuc.simple;

import gw.config.CommonServices;
import gw.config.ExecutionMode;
import gw.config.IMemoryMonitor;
import gw.config.IPlatformHelper;
import gw.config.Registry;
import gw.fs.FileFactory;
import gw.fs.IDirectory;
import gw.fs.IFile;
import gw.lang.gosuc.GosucDependency;
import gw.lang.gosuc.GosucModule;
import gw.lang.init.GosuInitialization;
import gw.lang.parser.ICoercionManager;
import gw.lang.parser.IParseIssue;
import gw.lang.parser.IParsedElement;
import gw.lang.parser.exceptions.ParseWarning;
import gw.lang.parser.statements.IClassFileStatement;
import gw.lang.parser.statements.IClassStatement;
import gw.lang.reflect.IEntityAccess;
import gw.lang.reflect.IType;
import gw.lang.reflect.TypeSystem;
import gw.lang.reflect.gs.IGosuClass;
import gw.lang.reflect.gs.ISourceFileHandle;
import gw.lang.reflect.module.IExecutionEnvironment;
import gw.lang.reflect.module.IFileSystem;
import gw.lang.reflect.module.IModule;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import static gw.lang.gosuc.simple.ICompilerDriver.ERROR;
import static gw.lang.gosuc.simple.ICompilerDriver.WARNING;

public class GosuCompiler implements IGosuCompiler {
  protected GosuInitialization _gosuInitialization;
  protected File _compilingSourceFile;

  public static void main(String[] args) {
//    log.debug("projectname=" + getProjectName());
//    log.debug("src/srcdir=" + getSrcdir());
//    log.debug("destdir=" + getDestdir());
//    log.debug("failOnError=" + getFailOnError());
//    log.debug("checkedArithmetic=" + isCheckedArithmetic());
//    log.debug("_compileClasspath=" + _compileClasspath);

    /*if(isCheckedArithmetic()) {
      System.setProperty("checkedArithmetic", "true");
    }*/
    if(args.length != 5) {
      System.err.println("GosuCompiler: Wrong parameters");
      System.exit(-1);
    }
    /*
       0: sourceRoots
       1: classpath
       2: outputPath
       3: explicit file list
       4: logger prefix
     */
    //List<String> sourceFolders, List<String> classpath, String outputPath
    ICompilerDriver driver = new SoutCompilerDriver();
    IGosuCompiler gosuc = new GosuCompiler();

    List<String> classpath = new ArrayList<>();
    classpath.addAll(getJreJars());
    classpath.addAll(Arrays.asList(args[1].split(":")));
//    classpath.addAll(getGosuJars());

//    if(!getProjectName().isEmpty()) {sy
//      startupMsg += " for " + getProjectName();
//    }
//    log.info(startupMsg);
//    log.debug("\tsourceFolders:" + Arrays.asList(getSrcdir().list()));
//    log.debug("\tclasspath:" + classpath);
//    log.debug("\toutputPath:" + getDestdir().getAbsolutePath());

    String[] srcRoots = args[0].split(":");

    final String projectName = args[4];
    String startupMsg = "Initializing Gosu compiler";
    if(projectName.isEmpty()) {
      startupMsg += " for " + projectName;
    }
    System.out.println(startupMsg);
    
    gosuc.initializeGosu(Arrays.asList(srcRoots), classpath, args[2]);

//    log.debug("About to compile these files:");
//    for(File file : compileList) {
//      log.debug("\t" + file.getAbsolutePath());
//    }

    List<File> allGosuSources = new ArrayList<>();
//    for(String srcRoot : srcRoots) {
//      allGosuSources.addAll(getGosuSources(srcRoot));
//    }
    for(String sourceFile : args[3].split(":")) {
      allGosuSources.add(new File(sourceFile));
    }

    for(File file : allGosuSources) {
      try {
        gosuc.compile(file, driver);
      } catch (Exception e) {
//        log.error(e.getMessage());
        throw new RuntimeException(e);
      }
    }

    gosuc.unitializeGosu();

    List<String> warnings = ((SoutCompilerDriver) driver).getWarnings();
    boolean errorsInCompilation = ((SoutCompilerDriver) driver).hasErrors();
    List<String> errors = ((SoutCompilerDriver) driver).getErrors();

    List<String> warningMessages = new ArrayList<>();
    List<String> errorMessages = new ArrayList<>();

    warnings.forEach(warning -> warningMessages.add("[WARNING] " + warning));
    int numWarnings = warningMessages.size();

    int numErrors = 0;
    if(errorsInCompilation) {
      errors.forEach(error -> errorMessages.add("[ERROR] " + error));
      numErrors = errorMessages.size();
    }

    boolean hasWarningsOrErrors = numWarnings > 0 || errorsInCompilation;
    StringBuilder sb;
    sb = new StringBuilder();
    sb.append(projectName.isEmpty() ? "Gosu compilation" : projectName);
    sb.append(" completed");
    if(hasWarningsOrErrors) {
      sb.append(" with ");
      if(numWarnings > 0) {
        sb.append(numWarnings).append(" warning").append(numWarnings == 1 ? "" : 's');
      }
      if(errorsInCompilation) {
        sb.append(numWarnings > 0 ? " and " : "");
        sb.append(numErrors).append(" error").append(numErrors == 1 ? "" : 's');
      }
    } else {
      sb.append(" successfully.");
    }

    if(hasWarningsOrErrors) {
      //log.warn(sb.toString());
      System.err.println(sb.toString());
    } else {
      System.out.println(sb.toString());
    }

    //log at most 100 warnings or errors
    warningMessages.subList(0, Math.min(warningMessages.size(), 100)).forEach(System.out::println);
    errorMessages.subList(0, Math.min(errorMessages.size(), 100)).forEach(System.err::println);

    if(errorsInCompilation) {
      System.err.println("Gosu compilation failed with errors; see compiler output for details.");
      System.exit(1);
//      if(getFailOnError()) {
//        buildError("Gosu compilation failed with errors; see compiler output for details.");
//      } else {
//        log.warn("Gosu Compiler: Ignoring compilation failure(s) as 'failOnError' was set to false");
//      }
    }
    //TODO any cleanup?
  }

  private static List<String> getGosuJars()
  {
    ///home/lboasso/.gradle/caches/modules-2/files-2.1/org.gosu-lang.gosu/gosu-core/1.10/596bf8615156116e83dfbd4933fce7bda934fdf4/gosu-core-1.10.jar
    ///home/lboasso/.gradle/caches/modules-2/files-2.1/org.gosu-lang.gosu/gosu-core-api/1.10/106324fe0dd18c214f2af509c78382d59a289e7/gosu-core-api-1.10.jar
    ///home/lboasso/.m2/repository/org/gosu-lang/gosu/managed/gw-asm-all/5.0.4/gw-asm-all-5.0.4.jar
    //return Arrays.asList( "../lib/gosu-core-1.10.jar", "../lib/gosu-core-api-1.10.jar", "../lib/gw-asm-all-5.0.4.jar" );
    return Arrays.asList( "/home/lboasso/projects/gosu-lang/gosu/target/gosu-1-X-SNAPSHOT-full/gosu-1-X-SNAPSHOT/lib/gosu-core-1-X-SNAPSHOT.jar",
                          "/home/lboasso/projects/gosu-lang/gosu/target/gosu-1-X-SNAPSHOT-full/gosu-1-X-SNAPSHOT/lib/gosu-core-api-1-X-SNAPSHOT.jar",
                          "/home/lboasso/projects/gosu-lang/gosu/target/gosu-1-X-SNAPSHOT-full/gosu-1-X-SNAPSHOT/lib/gw-asm-all-5.0.4.jar" );
  }

  /**
   * Get all JARs from the lib directory of the System's java.home property
   * @return List of absolute paths to all JRE libraries
   */
  private static List<String> getJreJars() {
    String javaHome = System.getProperty("java.home");
    java.nio.file.Path libsDir = FileSystems.getDefault().getPath(javaHome, "/lib");
    try {
      return Files.walk(libsDir)
        .filter( path -> path.toFile().isFile())
        .filter( path -> path.toString().endsWith(".jar"))
        .map( java.nio.file.Path::toString )
        .collect(Collectors.toList());
    } catch (SecurityException | IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

//  private static List<File> getGosuSources(String src) {
//    try {
//      return Files.walk(Paths.get(src))
//        .filter( path -> path.toFile().isFile())
//        .filter(path -> {
//          String fileName = path.toString();
//          return fileName.endsWith(".gs") || fileName.endsWith(".gsx") ||
//                fileName.endsWith(".gsp") || fileName.endsWith(".gst");})
//        .map(java.nio.file.Path::toFile)
//        .collect(Collectors.toList());
//    } catch (SecurityException | IOException e) {
//      e.printStackTrace();
//      throw new RuntimeException(e);
//    }
//  }

  public boolean compile(File sourceFile, ICompilerDriver driver) throws Exception {
    _compilingSourceFile = sourceFile;

    IType type = getType(_compilingSourceFile);
    if (type == null) {
      driver.sendCompileIssue(_compilingSourceFile, ERROR, 0, 0, 0, "Cannot find type in the Gosu Type System.");
      return false;
    }

    if (isCompilable(type)) {
      try {
        if(type.isValid()) {
          createOutputFiles((IGosuClass) type, driver);
        }
      } catch(CompilerDriverException ex) {
        driver.sendCompileIssue(_compilingSourceFile, ERROR, 0, 0, 0, ex.getMessage());
        return false;
      }
      // output warnings and errors - whether the type was valid or not
      IParsedElement classElement = ((IGosuClass) type).getClassStatement();
      IClassFileStatement classFileStatement = ((IClassStatement) classElement).getClassFileStatement();
      classElement = classFileStatement == null ? classElement : classFileStatement;
      ExecutionMode mode = CommonServices.getPlatformHelper().getExecutionMode();
      for (IParseIssue issue : classElement.getParseIssues()) {
        int category = issue instanceof ParseWarning ? WARNING : ERROR;
        String message = mode == ExecutionMode.IDE ? issue.getUIMessage() : issue.getConsoleMessage();
        driver.sendCompileIssue(_compilingSourceFile, category, issue.getTokenStart(), issue.getLine(), issue.getColumn(), message);
      }
    }

    return false;
  }

  private IType getType(File file) {
    IFile ifile = FileFactory.instance().getIFile(file);
    IModule module = TypeSystem.getGlobalModule();
    String[] typesForFile = TypeSystem.getTypesForFile(module, ifile);
    if (typesForFile.length != 0) {
      return TypeSystem.getByFullNameIfValid(typesForFile[0], module);
    }
    return null;
  }

  private boolean isCompilable(IType type) {
    IType doNotVerifyAnnotation = TypeSystem.getByFullNameIfValid("gw.testharness.DoNotVerifyResource");
    return type instanceof IGosuClass && !type.getTypeInfo().hasAnnotation(doNotVerifyAnnotation);
  }

  private void createOutputFiles(IGosuClass gsClass, ICompilerDriver driver) {
    IDirectory moduleOutputDirectory = TypeSystem.getGlobalModule().getOutputPath();
    if (moduleOutputDirectory == null) {
      throw new RuntimeException("Can't make class file, no output path defined.");
    }

    final String outRelativePath = gsClass.getName().replace('.', File.separatorChar) + ".class";
    File child = new File(moduleOutputDirectory.getPath().getFileSystemPathString());
    mkdirs(child);
    try {
      for (StringTokenizer tokenizer = new StringTokenizer(outRelativePath, File.separator + "/"); tokenizer.hasMoreTokens(); ) {
        String token = tokenizer.nextToken();
        child = new File(child, token);
        if (!child.exists()) {
          if (token.endsWith(".class")) {
            createNewFile(child);
          } else {
            mkDir(child);
          }
        }
      }
      populateClassFile(child, gsClass, driver);
      maybeCopySourceFile(child.getParentFile(), gsClass, _compilingSourceFile, driver);
    } catch (Throwable e) {
      driver.sendCompileIssue(_compilingSourceFile, ERROR, 0, 0, 0, combine("Cannot create .class files.", getStackTrace(e)));
    }
  }

  public static String getStackTrace(Throwable e) {
    StringWriter stringWriter = new StringWriter();
    e.printStackTrace(new PrintWriter(stringWriter));
    return stringWriter.toString();
  }

  private String toMessage(Throwable e) {
    String msg = e.getMessage();
    while (e.getCause() != null) {
      e = e.getCause();
      String newMsg = e.getMessage();
      if (newMsg != null) {
        msg = newMsg;
      }
    }
    return msg;
  }

  private String combine(String message1, String message2) {
    if (message1 == null) {
      message1 = "";
    } else {
      message1 = message1 + "\n";
    }
    return message1 + message2;
  }

  private void mkDir(File file) {
    file.mkdir();
  }

  private void mkdirs(File file) {
    file.mkdirs();
  }

  private void createNewFile(File file) throws IOException {
    file.createNewFile();
  }

  private void maybeCopySourceFile(File parent, IGosuClass gsClass, File sourceFile, ICompilerDriver driver) {
    ISourceFileHandle sfh = gsClass.getSourceFileHandle();
    IFile srcFile = sfh.getFile();
    if (srcFile != null) {
      File file = new File(srcFile.getPath().getFileSystemPathString());
      if (file.isFile()) {
        try {
          File destFile = new File(parent, file.getName());
          copyFile(file, destFile);
          driver.registerOutput(_compilingSourceFile, destFile);
        } catch (IOException e) {
          e.printStackTrace();
          driver.sendCompileIssue(sourceFile, ERROR, 0, 0, 0, "Cannot copy source file to output folder.");
        }
      }
    }
  }

  public void copyFile(File sourceFile, File destFile) throws IOException {
    if (sourceFile.isDirectory()) {
      mkdirs(destFile);
      return;
    }

    if (!destFile.exists()) {
      mkdirs(destFile.getParentFile());
      createNewFile(destFile);
    }

    try (FileChannel source = new FileInputStream(sourceFile).getChannel();
         FileChannel destination = new FileOutputStream(destFile).getChannel()) {
      destination.transferFrom(source, 0, source.size());
    }
  }

  private void populateClassFile( File outputFile, IGosuClass gosuClass, ICompilerDriver driver ) throws IOException {
    final byte[] bytes = TypeSystem.getGosuClassLoader().getBytes(gosuClass);
    try (OutputStream out = new FileOutputStream(outputFile)) {
      out.write(bytes);
      driver.registerOutput(_compilingSourceFile, outputFile);
    }
    for (IGosuClass innerClass : gosuClass.getInnerClasses()) {
      final String innerClassName = String.format("%s$%s.class", outputFile.getName().substring(0, outputFile.getName().lastIndexOf('.')), innerClass.getRelativeName());
      File innerClassFile = new File(outputFile.getParent(), innerClassName);
      if (innerClassFile.isFile()) {
        createNewFile(innerClassFile);
      }
      populateClassFile(innerClassFile, innerClass, driver);
    }
  }

  public long initializeGosu(List<String> sourceFolders, List<String> classpath, String outputPath) {
    final long start = System.currentTimeMillis();

    CommonServices.getKernel().redefineService_Privileged(IFileSystem.class, createFileSystemInstance());
    CommonServices.getKernel().redefineService_Privileged(IMemoryMonitor.class, new CompilerMemoryMonitor());
    CommonServices.getKernel().redefineService_Privileged(IPlatformHelper.class, new CompilerPlatformHelper());

    if ("gw".equals(System.getProperty("compiler.type"))) {
      try {
        IEntityAccess access = (IEntityAccess) Class.forName("gw.internal.gosu.parser.gwPlatform.GWEntityAccess").newInstance();
        ICoercionManager coercionManager = (ICoercionManager) Class.forName("gw.internal.gosu.parser.gwPlatform.GWCoercionManager").newInstance();
        CommonServices.getKernel().redefineService_Privileged(IEntityAccess.class, access);
        CommonServices.getKernel().redefineService_Privileged(ICoercionManager.class, coercionManager);
        Registry.instance().setAllowEntityQueires(true);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    IExecutionEnvironment execEnv = TypeSystem.getExecutionEnvironment();
    _gosuInitialization = GosuInitialization.instance(execEnv);
    GosucModule gosucModule = new GosucModule(
        IExecutionEnvironment.DEFAULT_SINGLE_MODULE_NAME, sourceFolders, classpath,
        outputPath, Collections.<GosucDependency>emptyList(), Collections.<String>emptyList());
    _gosuInitialization.initializeCompiler(gosucModule);

    return System.currentTimeMillis() - start;
  }

  private static IFileSystem createFileSystemInstance() {
    try {
      Class cls = Class.forName("gw.internal.gosu.module.fs.FileSystemImpl");
      Constructor m = cls.getConstructor(IFileSystem.CachingMode.class);
      return (IFileSystem) m.newInstance(IFileSystem.CachingMode.FULL_CACHING);
    } catch ( Exception e ) {
      throw new RuntimeException( e );
    }
  }

  public void unitializeGosu() {
    TypeSystem.shutdown(TypeSystem.getExecutionEnvironment());
    if (_gosuInitialization != null) {
      if (_gosuInitialization.isInitialized()) {
        _gosuInitialization.uninitializeCompiler();
      }
      _gosuInitialization = null;
    }
  }

  public boolean isPathIgnored(String sourceFile) {
    return CommonServices.getPlatformHelper().isPathIgnored(sourceFile);
  }
}
