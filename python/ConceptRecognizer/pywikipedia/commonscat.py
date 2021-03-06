#!/usr/bin/python
# -*- coding: utf-8  -*-
"""
With this tool you can add the template {{commonscat}} to categories.
The tool works by following the interwiki links. If the template is present on
another langauge page, the bot will use it.

You could probably use it at articles as well, but this isnt tested.

This bot uses pagegenerators to get a list of pages. The following options are
supported:

&params;

-always           Don't prompt you for each replacement. Warning message
                  has not to be confirmed. ATTENTION: Use this with care!

-summary:XYZ      Set the action summary message for the edit to XYZ,
                  otherwise it uses messages from add_text.py as default.

-checkcurrent     Work on all category pages that use the primary commonscat
                  template.

For example to go through all categories:
commonscat.py -start:Category:!
"""
# TODO:
"""
Commonscat bot:

Take a page. Follow the interwiki's and look for the commonscat template
*Found zero templates. Done.
*Found one template. Add this template
*Found more templates. Ask the user <- still have to implement this

TODO:
*Update interwiki's at commons
*Collect all possibilities also if local wiki already has link.
*Better support for other templates (translations) / redundant templates.
*Check mode, only check pages which already have the template
*More efficient like interwiki.py
*Possibility to update other languages in the same run

"""

#
# (C) Multichill, 2008-2009
# (C) Pywikipedia bot team, 2008-2010
#
# Distributed under the terms of the MIT license.
#
__version__ = '$Id: commonscat.py 8253 2010-06-06 16:53:30Z xqt $'
#

import wikipedia as pywikibot
import config, pagegenerators, add_text, re

docuReplacements = {
    '&params;': pagegenerators.parameterHelp
}

# Primary template, list of alternatives
# No entry needed if it is like _default
commonscatTemplates = {
    '_default': (u'Commonscat', []),
    'af' : (u'CommonsKategorie', [u'commonscat']),
    'ar' : (u'تصنيف كومنز', [u'Commonscat', u'تصنيف كومونز',
                             u'Commons cat', u'CommonsCat']),
    'az' : (u'CommonsKat', []),
    'bn' : (u'কমন্সক্যাট', [u'Commonscat']),
    'crh' : (u'CommonsKat', [u'Commonscat']),
    'cs' : (u'Commonscat', [u'Commons cat']),
    'da' : (u'Commonscat', [u'Commons cat', u'Commonskat', u'Commonscat2']),
    'de' : (u'Commonscat', [u'CommonsCat',]),
    'en' : (u'Commons category', [u'Commoncat', u'Commons2', u'Cms-catlist-up',
                                  u'Catlst commons', u'Commonscategory',
                                  u'Commonscat', u'Commons cat']),
    'es' : (u'Commonscat', [u'Ccat', u'Commons cat', u'Categoría Commons',
                            u'Commonscat-inline']),
    'eu' : (u'Commonskat', [u'Commonscat']),
    'fa' : (u'انبار-رده', [u'Commonscat', u'Commons cat',
                           u'انبار رده', u'Commons category']),
    'fr' : (u'Commonscat', [u'CommonsCat', u'Commons cat',
                            u'Commons category']),
    'frp' : (u'Commonscat', [u'CommonsCat']), 
    'ga' : (u'Catcómhaoin', [u'Commonscat']),
    'hi' : (u'Commonscat', [u'Commons2', u'Commons cat', u'Commons category']),
    'hu' : (u'Közvagyonkat', []),
    'hy' : (u'Commons cat', [u'Commonscat']),
    'id' : (u'Commonscat', [u'Commons cat', u'Commons2',
                            u'CommonsCat', u'Commons category']),
    'ja' : (u'Commonscat', [u'Commons cat', u'Commons category']),
    'jv' : (u'Commonscat', [u'Commons cat']),
    'kaa' : (u'Commons cat', [u'Commonscat']),
    'kk' : (u'Commonscat', [u'Commons2']),
    'ko' : (u'Commonscat', [u'Commons cat', u'공용분류']),
    'la' : (u'CommuniaCat', []),
    'mk' : (u'Ризница-врска', [u'Commonscat', u'Commons cat', u'CommonsCat',
                               u'Commons2', u'Commons category']),
    'ml' : (u'Commonscat', [u'Commons cat', u'Commons2']),
    'nn' : (u'Commonscat', [u'Commons cat']),
    'os' : (u'Commonscat', [u'Commons cat']),
    'pt' : (u'Commonscat', [u'Commons cat']),
    'ro' : (u'Commonscat', [u'Commons cat']),
    'ru' : (u'Commonscat', [u'Викисклад-кат']),
    'sl' : (u'Kategorija v Zbirki', [u'Commonscat', u'Kategorija v zbirki',
                                     u'Commons cat', u'Katzbirke']),
    'sv' : (u'Commonscat', [u'Commonscat-rad', u'Commonskat', u'Commons cat']),
    'sw' : (u'Commonscat', [u'Commons2', u'Commons cat']),
    'te' : (u'Commonscat', [u'Commons cat']),
    'tr' : (u'CommonsKat', [u'Commonscat', u'Commons cat']),
    'uk' : (u'Commonscat', [u'Commons cat', u'Category', u'Commonscat-inline']),
    'vi' : (u'Commonscat', [u'Commons2', u'Commons cat', u'Commons category',
                            u'Commons+cat']),
    'zh' : (u'Commonscat', [u'Commons cat']),
    'zh-classical' : (u'共享類', [u'Commonscat']),
    'zh-yue' : (u'同享類', [u'Commonscat', u'共享類 ', u'Commons cat']),
}

ignoreTemplates = {
    'af' : [u'commons'],
    'ar' : [u'تحويلة تصنيف', u'كومنز', u'كومونز', u'Commons'],
    'cs' : [u'Commons', u'Sestřičky', u'Sisterlinks'],
    'da' : [u'Commons', u'Commons left', u'Commons2', u'Commonsbilleder',
            u'Commonscat left', u'Commonscat2', u'GalleriCommons',
            u'Søsterlinks'],
    'de' : [u'Commons', u'Bauwerk-stil-kategorien',
            u'Bauwerk-funktion-kategorien',
            u'Kategoriesystem Augsburg-Infoleiste'],
    'en' : [u'Category redirect', u'Commons', u'Commonscat1A', u'Commoncats',
            u'Commonscat4Ra', u'Sisterlinks', u'Sisterlinkswp',
            u'Tracking category', u'Template category', u'Wikipedia category'],
    'eo' : [u'Commons',
            (u'Projekto/box', 'commons='),
            (u'Projekto', 'commons='),
            (u'Projektoj', 'commons='),
            (u'Projektoj', 'commonscat=')],
    'es' : [u'Commons', u'IprCommonscat'],
    'eu' : [u'Commons'],
    'fa' : [u'Commons', u'ویکی‌انبار'],
    'fi' : [u'Commonscat-rivi', u'Commons-rivi', u'Commons'],
    'fr' : [u'Commons', u'Commons-inline', (u'Autres projets', 'commons=')],
    'fy' : [u'Commons', u'CommonsLyts'],
    'hr' : [u'Commons', (u'WProjekti', 'commonscat=')],
    'it' : [(u'Ip', 'commons='), (u'Interprogetto', 'commons=')],
    'ja' : [u'CommonscatS', u'SisterlinksN', u'Interwikicat'],
    'nds-nl' : [u'Commons'],
    'nl' : [u'Commons', u'Commonsklein', u'Commonscatklein', u'Catbeg',
            u'Catsjab', u'Catwiki'],
    'om' : [u'Commons'],
    'pt' : [u'Correlatos'],
    'ru' : [u'Навигация'],
}

msg_change = {
    'de': u'Bot: Ändere commonscat link von [[:Commons:Category:%(oldcat)s|%(oldcat)s]] zu [[:Commons:Category:%(newcat)s|%(newcat)s]]',
    'en': u'Robot: Changing commonscat link from [[:Commons:Category:%(oldcat)s|%(oldcat)s]] to [[:Commons:Category:%(newcat)s|%(newcat)s]]',
    'fr': u'Robot: Changé commonscat link de [[:Commons:Category:%(oldcat)s|%(oldcat)s]] à [[:Commons:Category:%(newcat)s|%(newcat)s]]',
    'pdc': u'Waddefresser: commonscat Gleecher vun [[:Commons:Category:%(oldcat)s|%(oldcat)s]] nooch [[:Commons:Category:%(newcat)s|%(newcat)s]] geennert',
}

def getCommonscatTemplate (lang = None):
    '''
    Get the template name in a language. Expects the language code.
    Return as tuple containing the primary template and it's alternatives
    '''
    if lang in commonscatTemplates:
        return  commonscatTemplates[lang]
    else:
        return commonscatTemplates[u'_default']

def skipPage(page):
    '''
    Do we want to skip this page?
    '''
    if page.site().language() in ignoreTemplates:
        templatesInThePage = page.templates()
        templatesWithParams = page.templatesWithParams()
        for template in ignoreTemplates[page.site().language()]:
            if type(template) != tuple:
                if template in templatesInThePage:
                    return True
            else:
                for (inPageTemplate, param) in templatesWithParams:
                    if inPageTemplate == template[0] \
                       and template[1] in param[0]:
                        return True
    return False

def updateInterwiki (wikipediaPage = None, commonsPage = None):
    '''
    Update the interwiki's at commons from a wikipedia page. The bot just
    replaces the interwiki links at the commons page with the interwiki's from
    the wikipedia page. This should probably be more intelligent. We could use
    add all the interwiki's and remove duplicates. Or only remove language links
    if multiple language links to the same language exist.

    This function is disabled for the moment untill i figure out what the best
    way is to update the interwiki's.
    '''
    interwikis = {}
    comment= u''
    interwikilist = wikipediaPage.interwiki()
    interwikilist.append(wikipediaPage)

    for interwikiPage in interwikilist:
        interwikis[interwikiPage.site()]=interwikiPage
    oldtext = commonsPage.get()
    # The commonssite object doesnt work with interwiki's
    newtext = pywikibot.replaceLanguageLinks(oldtext, interwikis,
                                             pywikibot.getSite(u'nl'))
    comment = u'Updating interwiki\'s from [[' + \
              wikipediaPage.site().language()  + \
              u':' + wikipediaPage.title() + u']]'

    if newtext != oldtext:
        #This doesnt seem to work. Newtext has some trailing whitespace
        pywikibot.showDiff(oldtext, newtext)
        commonsPage.put(newtext=newtext, comment=comment)

def addCommonscat (page = None, summary = None, always = False):
    '''
    Take a page. Go to all the interwiki page looking for a commonscat template.
    When all the interwiki's links are checked and a proper category is found
    add it to the page.
    '''
    pywikibot.output(u'Working on ' + page.title());
    #Get the right templates for this page
    primaryCommonscat, commonscatAlternatives = getCommonscatTemplate(
        page.site().language())
    commonscatLink = getCommonscatLink (page)
    if commonscatLink:
        pywikibot.output(u'Commonscat template is already on %s'
                         % page.title())
        (currentCommonscatTemplate, currentCommonscatTarget) = commonscatLink
        checkedCommonscatTarget = checkCommonscatLink(currentCommonscatTarget)
        if (currentCommonscatTarget==checkedCommonscatTarget):
            #The current commonscat link is good
            pywikibot.output(u'Commonscat link at %s to Category:%s is ok'
                             % (page.title() , currentCommonscatTarget));
            return (True, always)
        elif checkedCommonscatTarget!=u'':
            #We have a new Commonscat link, replace the old one
            changeCommonscat(page, currentCommonscatTemplate,
                             currentCommonscatTarget, primaryCommonscat,
                             checkedCommonscatTarget)
            return (True, always)
        else:
            #Commonscat link is wrong
            commonscatLink = findCommonscatLink(page)
            if (commonscatLink!=u''):
                changeCommonscat (page, currentCommonscatTemplate,
                                  currentCommonscatTarget, primaryCommonscat,
                                  commonscatLink)
            #else
            #Should i remove the commonscat link?

    elif skipPage(page):
        pywikibot.output("Found a template in the skip list. Skipping %s"
                         % page.title());
    else:
        commonscatLink = findCommonscatLink(page)
        if (commonscatLink!=u''):
            textToAdd = u'{{' + primaryCommonscat + u'|' + commonscatLink + u'}}'
            (success, status, always) = add_text.add_text(page, textToAdd,
                                                          summary, None, None,
                                                          always);
            return (True, always);

    return (True, always);

def changeCommonscat (page=None, oldtemplate=u'', oldcat=u'', newtemplate=u'',
                      newcat=u''):
    '''
    Change the current commonscat template and target. 
    '''
    newtext = re.sub(u'(?i)\{\{' + oldtemplate + u'\|?[^}]*\}\}',
                     u'{{' + newtemplate + u'|' + newcat + u'}}',
                     page.get())
    comment = pywikibot.translate(page.site(), msg_change) % {'oldcat':oldcat, 'newcat':newcat}
    pywikibot.showDiff(page.get(), newtext)
    page.put(newtext, comment)

def findCommonscatLink (page=None):
    for ipage in page.interwiki():
        try:
            if(ipage.exists() and not ipage.isRedirectPage()
               and not ipage.isDisambig()):
                commonscatLink = getCommonscatLink (ipage)
                if commonscatLink:
                    (currentCommonscatTemplate, possibleCommonscat) = commonscatLink
                    checkedCommonscat = checkCommonscatLink(possibleCommonscat)
                    if (checkedCommonscat!= u''):
                        pywikibot.output(
                            u"Found link for %s at [[%s:%s]] to %s."
                            % (page.title(), ipage.site().language(),
                               ipage.title(), checkedCommonscat))
                        return checkedCommonscat
        except pywikibot.BadTitle:
            #The interwiki was incorrect
            return u''
    return u''


def getCommonscatLink (wikipediaPage=None):
    '''
    Go through the page and return a tuple of (<templatename>, <target>)
    '''
    primaryCommonscat, commonscatAlternatives = getCommonscatTemplate(
        wikipediaPage.site().language())
    commonscatTemplate =u''
    commonscatTarget = u''
    #See if commonscat is present

    for template in wikipediaPage.templatesWithParams():
        if template[0]==primaryCommonscat \
           or template[0] in commonscatAlternatives:
            commonscatTemplate = template[0]
            if (len(template[1]) > 0):
                commonscatTarget = template[1][0]
            else:
                commonscatTarget = wikipediaPage.titleWithoutNamespace()
            return (commonscatTemplate, commonscatTarget)

    return None

def checkCommonscatLink (name = ""):
    '''
    This function will retun the name of a valid commons category
    If the page is a redirect this function tries to follow it.
    If the page doesnt exists the function will return an empty string
    '''
    if pywikibot.verbose:
        pywikibot.output("getCommonscat: " + name )
    try:
        #This can throw a pywikibot.BadTitle
        commonsPage = pywikibot.Page(pywikibot.getSite("commons", "commons"),
                                     "Category:" + name)

        if not commonsPage.exists():
            if pywikibot.verbose:
                pywikibot.output(u"getCommonscat: The category doesnt exist.")
            return u''
        elif commonsPage.isRedirectPage():
            if pywikibot.verbose:
                pywikibot.output(u"getCommonscat: The category is a redirect")
            return checkCommonscatLink(
                commonsPage.getRedirectTarget().titleWithoutNamespace())
        elif "Category redirect" in commonsPage.templates():
            if pywikibot.verbose:
                pywikibot.output(
                    u"getCommonscat: The category is a category redirect")
            for template in commonsPage.templatesWithParams():
                if ((template[0]=="Category redirect")
                    and (len(template[1]) > 0)):
                    return checkCommonscatLink(template[1][0])
        elif commonsPage.isDisambig():
            if pywikibot.verbose:
                pywikibot.output(
                    u"getCommonscat: The category is disambiguation")
            return u''
        else:
            return commonsPage.titleWithoutNamespace()
    except pywikibot.BadTitle:
        #Funky title so not correct
        return u''
    except pywikibot.PageNotFound:
        return u''

def main():
    '''
    Parse the command line arguments and get a pagegenerator to work on.
    Iterate through all the pages.
    '''
    summary = None; generator = None; checkcurrent = False; always = False
    ns = []
    ns.append(14)
    # Load a lot of default generators
    genFactory = pagegenerators.GeneratorFactory()

    for arg in pywikibot.handleArgs():
        if arg.startswith('-summary'):
            if len(arg) == 8:
                summary = pywikibot.input(u'What summary do you want to use?')
            else:
                summary = arg[9:]
        elif arg.startswith('-checkcurrent'):
            checkcurrent = True
            primaryCommonscat, commonscatAlternatives = getCommonscatTemplate(
                pywikibot.getSite().language())
            generator = pagegenerators.NamespaceFilterPageGenerator(
                pagegenerators.ReferringPageGenerator(
                    pywikibot.Page(pywikibot.getSite(),
                                   u'Template:' + primaryCommonscat),
                    onlyTemplateInclusion=True), ns)

        elif arg == '-always':
            always = True
        else:
            genFactory.handleArg(arg)

    if not generator:
        generator = genFactory.getCombinedGenerator()
    if not generator:
        raise add_text.NoEnoughData(
u'You have to specify the generator you want to use for the script!')

    pregenerator = pagegenerators.PreloadingGenerator(generator)

    for page in pregenerator:
        if not page.exists():
           pywikibot.output(u'Page %s does not exist. Skipping.'
                            % page.aslink())
        elif page.isRedirectPage():
           pywikibot.output(u'Page %s is a redirect. Skipping.' % page.aslink())
        elif page.isCategoryRedirect():
           pywikibot.output(u'Page %s is a category redirect. Skipping.'
                            % page.aslink())
        elif page.isDisambig():
           pywikibot.output(u'Page %s is a disambiguation. Skipping.'
                            % page.aslink())
        else:
            (status, always) = addCommonscat(page, summary, always)

if __name__ == "__main__":
    try:
        main()
    finally:
        pywikibot.stopme()
