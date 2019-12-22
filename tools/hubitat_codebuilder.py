#  Copyright 2019 Markus Liljergren
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#  http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
import os
from pathlib import Path
import re
import pprint
import yaml
import ruamel.yaml
import logging
import winsound
import datetime
from colorama import init, Fore, Style

# Local imports
from hubitat_driver_snippets import *
from hubitat_driver_snippets_parser import *

# Custom formatter
class HubitatCodeBuilderLogFormatter(logging.Formatter):

    def __init__(self, fmt_default="%(time_elapsed)-11s:%(name)-20s:%(levelname)5s: %(msg)s", 
                    fmt_debug="%(time_elapsed)-11s:%(name)-20s:%(levelname)5s:%(lineno)4d:%(funcName)s: %(msg)s", 
                    fmt_error="%(time_elapsed)-11s:%(name)-20s:%(levelname)5s:%(lineno)4d:%(funcName)s: %(msg)s", 
                    error_beep=True, default_color=Fore.GREEN, debug_color=Fore.YELLOW, error_color=Fore.RED,):
        init() # This is the init for Colorama
        # Another format to use: '%(asctime)s:%(name)20s:%(levelname)5s: %(message)s'
        self._error_beep = error_beep
        self._init_time = datetime.datetime.utcnow()
        self._formatter_debug = logging.Formatter(fmt=fmt_debug)
        self._formatter_error = logging.Formatter(fmt=fmt_error)
        self._default_color = default_color
        self._debug_color = debug_color
        self._error_color = error_color
        super().__init__(fmt=fmt_default)

    def format(self, record):
        now = datetime.datetime.utcnow()
        try:
            delta = now - self._init_time
        except AttributeError:
            delta = 0

        # First add the elapsed time
        record.time_elapsed = '{0:.2f}ms'.format(delta.total_seconds() * 1000)
        
        # Now add our colors
        if record.levelno == logging.DEBUG:
            if(self._debug_color != None):
                res = self._debug_color + self._formatter_debug.format(record) + Style.RESET_ALL
            else:
                res = self._formatter_debug.format(record)
        elif record.levelno == logging.ERROR:
            if(self._error_color != None):
                res = self._error_color + self._formatter_error.format(record) + Style.RESET_ALL
            else:
                res = self._formatter_error.format(record)
            if(self._error_beep):
                winsound.Beep(500, 300)
        else:
            if(self._default_color != None):
                res = self._default_color + super().format(record) + Style.RESET_ALL
            else:
                res = super().format(record)

        return(res)

class HubitatCodeBuilderError(Exception):
   """HubitatCodeBuilder Base Exception Class"""
   pass

class HubitatCodeBuilder:

    def __init__(self, app_dir = Path('./apps'), app_build_dir = Path('./apps/expanded'), \
                 driver_dir = Path('./drivers'), driver_build_dir = Path('./drivers/expanded'), \
                 build_suffix = '-expanded'    ):
        self.app_dir = Path(app_dir)
        self.app_build_dir = Path(app_build_dir)
        self.driver_dir = Path(driver_dir)
        self.driver_build_dir = Path(driver_build_dir)
        self.build_suffix = build_suffix
        self.log = logging.getLogger(__name__)
        my_locals = locals().copy()
        my_locals.pop('self')
        self.log.debug('Settings: {}'.format(str(my_locals)))

    def getHelperFunctions(self, helper_function_type):
        r = ''
        f = './helpers/helpers-' + helper_function_type + '.groovy'
        if(os.path.isfile(f)):
            with open (f, "r") as rd:
                for l in rd:
                    r += l
        else:
            # Yes, this should be specific, but it doesn't matter here...
            raise HubitatCodeBuilderError("Helper function type '" + helper_function_type + "' can't be included! File doesn't exist!")
        return(r)

    def getOutputGroovyFile(self, input_groovy_file, alternate_output_filename = None):
        #self.log.debug('Using "' + str(input_groovy_file) + '" to get path for "' + str(output_groovy_dir) + '"...')
        #self.log.debug('Filename stem: ' + input_groovy_file.stem)
        #self.log.debug('Filename suffix: ' + input_groovy_file.suffix)
        input_groovy_file = Path(input_groovy_file)
        if(alternate_output_filename != None):
            output_groovy_file = Path(str(alternate_output_filename) + self.build_suffix + str(input_groovy_file.suffix))
        else:
            output_groovy_file = Path(str(input_groovy_file.stem) + self.build_suffix + str(input_groovy_file.suffix))
        #print('output_groovy_file: ' + str(output_groovy_file))
        return(output_groovy_file)

    def _checkFordefinition_string(self, l, alternate_name = None, alternate_namespace = None, alternate_vid = None):
        definition_position = l.find('definition (')
        if(definition_position != -1):
            ds = l[definition_position+11:].strip()
            # On all my drivers the definition row ends with ") {"
            self.log.debug('Parsing Definition statement')
            #print('{'+ds[1:-3]+'}')
            definition_dict = yaml.load(('{'+ds[1:-3]+' }').replace(':', ': '), Loader=yaml.FullLoader)
            self.log.debug(definition_dict)
            if(alternate_name != None):
                definition_dict['name'] = alternate_name
            if(alternate_namespace != None):
                definition_dict['namespace'] = alternate_namespace
            if(alternate_vid != None):
                definition_dict['vid'] = alternate_vid
            #print(definition_dict)
            # Process this string
            # (name: "Tasmota - Tuya Wifi Touch Switch TEST (Child)", namespace: "tasmota", author: "Markus Liljergren") {
            #PATTERN = re.compile(r'''((?:[^,"']|"[^"]*"|'[^']*')+)''')
            #PATTERN2 = re.compile(r'''((?:[^(){}:"']|"[^"]*"|'[^']*')+)''')
            #l1 = PATTERN.split(ds)[1::2]
            #d = {}
            #for p1 in l1:
            #    p1 = p1.strip()
            #    i = 0
            #    previousp2 = None
            #    for p2 in PATTERN2.split(p1)[1::2]:
            #        p2 = p2.strip()
            #        if(p2 != ''):
            #            if(i==0):
            #                previousp2 = p2.strip('"')
            #            else:
            #                #self.log.debug('"' + previousp2 + '"="' + p2.strip('"') + '"')
            #                d[previousp2] = p2.strip('"')
            #            i += 1
            #self.log.debug(d)
            definition_dict_original = definition_dict.copy()
            ds = '[' + str(definition_dict)[1:-1] + ']'
            for k in definition_dict:
                definition_dict[k] = '"x' + definition_dict[k] + 'x"'
            new_definition = (l[:definition_position]) + 'definition (' + yaml.dump(definition_dict, default_flow_style=False, sort_keys=False ).replace('\'"x', '"').replace('x"\'', '"').replace('\n', ', ')[:-2] + ') {\n'
            #print(new_definition)
            output = 'def getDeviceInfoByName(infoName) { \n' + \
                '    // DO NOT EDIT: This is generated from the metadata!\n' + \
                '    // TODO: Figure out how to get this from Hubitat instead of generating this?\n' + \
                '    deviceInfo = ' + ds + '\n' + \
                '    return(deviceInfo[infoName])\n' + \
                '}'
            #new_definition = l
            return(new_definition, output, definition_dict_original)
        else:
            return(None)

    def getBuildDir(self, code_type):
        if(code_type == 'driver'):
            return(self.driver_build_dir)
        elif(code_type == 'app'):
            return(self.app_build_dir)
        else:
            raise HubitatCodeBuilderError('Incorrect code_type: ' + str(code_type))
    
    def getInputDir(self, code_type):
        if(code_type == 'driver'):
            return(self.driver_dir)
        elif(code_type == 'app'):
            return(self.app_dir)
        else:
            raise HubitatCodeBuilderError('Incorrect code_type: ' + str(code_type))
    
    def _runEvalCmd(self, eval_cmd, definition_string, alternate_template, alternate_module):
        output = eval_cmd
        found = False
        if(eval_cmd == 'getDeviceInfoFunction()'):
            self.log.debug("Executing getDeviceInfoFunction()...")
            if(definition_string == None):
                raise HubitatCodeBuilderError('ERROR: Missing/incorrect Definition in file!')
            output = definition_string
            found = True
        else:
            try:
                (found, output) = self._runEvalCmdAdditional(eval_cmd, definition_string, alternate_template, alternate_module)
                if(found == False):
                    output = eval_cmd
            except AttributeError:
                #print(str(e))
                found = False
        if(found == False):
            try:
                output = eval(eval_cmd)
            except NameError:
                try:
                    output = eval('self.' + eval_cmd)
                except AttributeError:
                    output = eval('self._' + eval_cmd)
        return(output)

    def expandGroovyFile(self, input_groovy_file, code_type = 'driver', alternate_output_filename = None, \
                        alternate_name = None, alternate_namespace = None, alternate_vid = None, \
                        alternate_template = None, alternate_module = None):
        input_groovy_file = Path(input_groovy_file)
        output_groovy_file = self.getOutputGroovyFile(input_groovy_file, alternate_output_filename)
        r = {'file': output_groovy_file, 'name': ''}
        
        self.log.debug('Expanding "' + str(input_groovy_file) + '" to "' + str(output_groovy_file) + '"...')
        definition_string = None
        self.log.debug('Build dir: ' + str(self.getBuildDir(code_type) / output_groovy_file))
        with open (self.getBuildDir(code_type) / output_groovy_file, "w") as wd:
            with open (self.getInputDir(code_type) / input_groovy_file, "r") as rd:
                # Read lines in loop
                for l in rd:
                    if(definition_string == None):
                        definition_string = self._checkFordefinition_string(l, alternate_name = alternate_name, alternate_namespace = alternate_namespace, alternate_vid = alternate_vid)
                        if(definition_string != None):
                            (l, definition_string, definition_dict_original) = definition_string
                            r['name'] = definition_dict_original['name']
                    includePosition = l.find('#!include:')
                    if(includePosition != -1):
                        eval_cmd = l[includePosition+10:].strip()
                        output = self._runEvalCmd(eval_cmd, definition_string, alternate_template, alternate_module)
                        if(includePosition > 0):
                            i = 0
                            wd.write(l[:includePosition])
                            for nl in output.splitlines():
                                if i != 0:
                                    wd.write(' ' * (includePosition) + nl + '\n')
                                else:
                                    wd.write(nl + '\n')
                                i += 1
                        else:
                            wd.write(output + '\n')
                    else:
                        wd.write(l)
                    #print(l.strip())
        self.log.info('DONE expanding "' + input_groovy_file.name + '" to "' + output_groovy_file.name + '"!')
        return(r)

