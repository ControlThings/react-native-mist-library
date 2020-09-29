
Pod::Spec.new do |s|
  s.name         = "react-native-mist-library"
  s.version      = "2.0.0"
  s.summary      = "MistApi wrapper for react native."
  s.description  = <<-DESC
                  RNMistLibrary
                   DESC
  s.homepage     = "https://github.com/ControlThings/react-native-mist-library"
  s.license      = "Apache 2.0"
  s.author             = { "author" => "author@domain.cn" }
  s.platform     = :ios, "11.0"
  s.source       = { :git => "https://github.com/ControlThings/react-native-mist-library", :tag => "2.0.0-release" }
  s.source_files  = "**/*.{h,m}"
  s.requires_arc = true
  s.ios.vendored_libraries = 'lib/libMistApi.a'

  s.dependency "React"
  #s.dependency "others"

end

  
